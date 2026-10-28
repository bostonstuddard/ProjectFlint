package dev.projectflint.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Leather-bound, searchable, paginated Project Flint field guide; no recipes or server state required. */
public final class ProjectFlintFieldGuideScreen extends Screen {
    private static final int EDGE = 0xFF37251C;
    private static final int LEATHER = 0xFF55382B;
    private static final int PAPER = 0xFFE4D5B2;
    private static final int INK = 0xFF362920;
    private static final int GOLD = 0xFFC69A50;
    private static final int ROW_HEIGHT = 19;

    private final Screen parent;
    private final List<ProjectFlintFieldGuideContent.Chapter> chapters = ProjectFlintFieldGuideContent.chapters();
    private final List<Item> allItems;
    private EditBox search;
    private Button previousPage;
    private Button nextPage;
    private int selected;
    private int page;
    private int chapterOffset;
    private int bodyScroll;

    public ProjectFlintFieldGuideScreen(Screen parent) {
        super(Component.literal("Project Flint Field Guide"));
        this.parent = parent;
        allItems = BuiltInRegistries.ITEM.stream()
            .filter(item -> "projectflint".equals(BuiltInRegistries.ITEM.getKey(item).getNamespace()))
            .sorted(Comparator.comparing(item -> new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT)))
            .toList();
    }

    private int panelWidth() { return Math.min(518, width - 18); }
    private int panelHeight() { return Math.min(346, height - 14); }
    private int left() { return (width - panelWidth()) / 2; }
    private int top() { return (height - panelHeight()) / 2; }
    private int spine() { return left() + Math.min(156, panelWidth() / 3); }
    private int visibleRows() { return Math.max(3, (panelHeight() - 112) / ROW_HEIGHT); }
    private int atlasRows() { return Math.max(3, (panelHeight() - 135) / 21); }
    private String query() { return search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT); }

    private List<Item> matchingItems() {
        String needle = query();
        if (needle.isBlank()) return allItems;
        return allItems.stream().filter(item -> {
            String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
            return name.contains(needle) || BuiltInRegistries.ITEM.getKey(item).getPath().contains(needle);
        }).toList();
    }

    private List<ProjectFlintFieldGuideContent.Chapter> visibleChapters() {
        String needle = query();
        boolean foundItems = !matchingItems().isEmpty();
        return chapters.stream().filter(ch -> ch.matches(needle)
            || (ch.title().equals("Item Atlas") && foundItems)).toList();
    }

    private ProjectFlintFieldGuideContent.Chapter activeChapter(List<ProjectFlintFieldGuideContent.Chapter> shown) {
        return shown.isEmpty() ? null : shown.get(Math.max(0, Math.min(selected, shown.size() - 1)));
    }

    private int pageCount(ProjectFlintFieldGuideContent.Chapter chapter) {
        if (chapter == null) return 1;
        if (chapter.title().equals("Item Atlas")) return Math.max(1, (matchingItems().size() + atlasRows() - 1) / atlasRows());
        return Math.max(1, chapter.pages().size());
    }

    @Override
    protected void init() {
        int x = left(), y = top(), w = panelWidth(), h = panelHeight(), border = spine();
        search = addRenderableWidget(new EditBox(font, x + 11, y + 40, border - x - 22, 19, Component.literal("Find chapters")));
        search.setMaxLength(80);
        search.setHint(Component.literal("Find a topic..."));
        search.setResponder(value -> { selected = 0; page = 0; chapterOffset = 0; bodyScroll = 0; updateButtons(); });

        previousPage = addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> changePage(-1))
            .bounds(border + 15, y + h - 31, 63, 20).build());
        nextPage = addRenderableWidget(Button.builder(Component.literal("Next >"), b -> changePage(1))
            .bounds(x + w - 154, y + h - 31, 63, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(x + w - 78, y + h - 31, 65, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Recipes (K)"), b -> ProjectFlintRecipeGuideScreen.openGlobal(this))
            .bounds(x + 11, y + h - 31, border - x - 22, 20).build());
        updateButtons();
    }

    private void changePage(int direction) {
        List<ProjectFlintFieldGuideContent.Chapter> shown = visibleChapters();
        int max = pageCount(activeChapter(shown));
        page = Math.max(0, Math.min(max - 1, page + direction));
        bodyScroll = 0;
        updateButtons();
    }

    private void selectChapter(int index) {
        selected = index;
        page = 0;
        bodyScroll = 0;
        updateButtons();
    }

    private void updateButtons() {
        if (previousPage == null || nextPage == null) return;
        int pages = pageCount(activeChapter(visibleChapters()));
        page = Math.min(page, pages - 1);
        previousPage.active = page > 0;
        nextPage.active = page + 1 < pages;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        double mx = event.x(), my = event.y();
        int x = left(), y = top(), border = spine();
        if (mx >= x + 8 && mx < border - 4 && my >= y + 74 && my < y + panelHeight() - 43) {
            int row = ((int) my - (y + 74)) / ROW_HEIGHT;
            int index = chapterOffset + row;
            if (index >= 0 && index < visibleChapters().size()) { selectChapter(index); return true; }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        int x = left(), y = top();
        if (mx > x && mx < spine() && my > y + 70 && my < y + panelHeight() - 38) {
            int maximum = Math.max(0, visibleChapters().size() - visibleRows());
            chapterOffset = Math.max(0, Math.min(maximum, chapterOffset - (int) Math.signum(vertical) * 3));
            return true;
        }
        if (mx > spine() && mx < x + panelWidth() && my > y + 57 && my < y + panelHeight() - 38) {
            bodyScroll = Math.max(0, bodyScroll - (int) Math.signum(vertical) * 3);
            return true;
        }
        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        // Minecraft 1.21.11 permits only one background blur per frame. Other
        // screens/render hooks may have already requested it before this method.
        // The book is opaque, so a lightweight translucent dimmer is sufficient
        // and does not re-enter Screen.renderBackground's blur pipeline.
        g.fill(0, 0, width, height, 0xA0000000);
        int x = left(), y = top(), w = panelWidth(), h = panelHeight(), border = spine();
        g.fill(x, y, x + w, y + h, EDGE);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, LEATHER);
        g.fill(x + 7, y + 7, border - 2, y + h - 7, 0xFF473024);
        g.fill(border + 2, y + 7, x + w - 7, y + h - 7, PAPER);
        g.fill(border - 2, y + 5, border + 3, y + h - 5, GOLD);
        g.fill(x + 9, y + 30, border - 5, y + 32, GOLD);
        g.drawCenteredString(font, "FIELD GUIDE", x + (border - x) / 2, y + 13, 0xFFE9CD91);
        g.drawString(font, "CHAPTERS", x + 14, y + 64, 0xFFE5C78E, false);

        List<ProjectFlintFieldGuideContent.Chapter> shown = visibleChapters();
        if (selected >= shown.size()) selected = Math.max(0, shown.size() - 1);
        ProjectFlintFieldGuideContent.Chapter chapter = activeChapter(shown);
        if (chapter == null) {
            g.drawWordWrap(font, Component.literal("No matching chapter or item. Try another search."), border + 16, y + 83, w - (border - x) - 32, INK);
        } else {
            int count = visibleRows();
            if (chapterOffset >= shown.size()) chapterOffset = Math.max(0, shown.size() - count);
            for (int i = chapterOffset; i < Math.min(shown.size(), chapterOffset + count); i++) {
                ProjectFlintFieldGuideContent.Chapter entry = shown.get(i);
                int ry = y + 74 + (i - chapterOffset) * ROW_HEIGHT;
                if (i == selected) g.fill(x + 10, ry - 2, border - 5, ry + 17, 0xFF977049);
                g.renderItem(new ItemStack(entry.icon()), x + 12, ry - 1);
                String label = font.plainSubstrByWidth(entry.title(), Math.max(30, border - x - 41));
                g.drawString(font, label, x + 31, ry + 3, i == selected ? 0xFF21150F : 0xFFF3DEB8, false);
            }
            if (shown.size() > count)
                g.drawCenteredString(font, (chapterOffset + 1) + "-" + Math.min(shown.size(), chapterOffset + count) + " / " + shown.size(),
                    x + (border - x) / 2, y + h - 44, 0xFFE1C18B);

            g.drawString(font, chapter.section(), border + 19, y + 15, 0xFF85612E, false);
            g.drawString(font, font.plainSubstrByWidth(chapter.title(), w - (border - x) - 74), border + 19, y + 32, INK, false);
            g.renderItem(new ItemStack(chapter.icon()), x + w - 36, y + 23);
            g.fill(border + 17, y + 53, x + w - 17, y + 55, GOLD);
            int maxPages = pageCount(chapter);
            if (page >= maxPages) page = maxPages - 1;
            int bodyX = border + 17, bodyY = y + 67, bodyWidth = x + w - bodyX - 19;
            int bodyBottom = y + h - 47;
            g.enableScissor(bodyX, bodyY, x + w - 15, bodyBottom);
            if (chapter.title().equals("Item Atlas")) {
                renderAtlas(g, mx, my, bodyX, bodyY, bodyWidth, bodyBottom);
            } else {
                String prose = chapter.pages().get(page);
                renderProse(g, prose, bodyX, bodyY, bodyWidth, bodyBottom);
            }
            g.disableScissor();
            g.drawCenteredString(font, "" + (page + 1) + " / " + maxPages, (border + x + w) / 2, y + h - 38, 0xFF754E29);
        }
        super.render(g, mx, my, partialTick);
    }

    private void renderProse(GuiGraphics g, String prose, int x, int y, int width, int bottom) {
        int line = 0;
        int firstVisible = Math.max(0, bodyScroll);
        for (String paragraph : prose.split("\\n\\n", -1)) {
            for (var row : font.split(Component.literal(paragraph), width)) {
                if (line >= firstVisible) {
                    int cy = y + (line - firstVisible) * 12;
                    if (cy < bottom) g.drawString(font, row, x, cy, INK, false);
                }
                line++;
            }
            line++;
        }
        int maxLines = Math.max(1, (bottom - y) / 12);
        bodyScroll = Math.max(0, Math.min(bodyScroll, Math.max(0, line - maxLines)));
    }

    private void renderAtlas(GuiGraphics g, int mx, int my, int x, int y, int width, int bottom) {
        List<Item> items = matchingItems();
        if (items.isEmpty()) { g.drawString(font, "No registered items match.", x, y, INK); return; }
        int rows = atlasRows();
        int start = page * rows;
        for (int i = start; i < Math.min(items.size(), start + rows); i++) {
            Item item = items.get(i);
            int ry = y + (i - start) * 21;
            if (ry + 18 > bottom) break;
            ItemStack stack = new ItemStack(item);
            g.renderItem(stack, x + 1, ry);
            String name = font.plainSubstrByWidth(stack.getHoverName().getString(), width - 27);
            String id = BuiltInRegistries.ITEM.getKey(item).getPath();
            g.drawString(font, name, x + 23, ry, INK, false);
            g.drawString(font, font.plainSubstrByWidth(id, width - 27), x + 23, ry + 10, 0xFF876A4E, false);
            if (mx >= x && mx < x + 20 && my >= ry && my < ry + 18)
                g.setTooltipForNextFrame(font, stack, mx, my);
        }
    }

    @Override
    public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
