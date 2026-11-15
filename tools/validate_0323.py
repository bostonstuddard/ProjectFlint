#!/usr/bin/env python3
"""Non-runtime Project Flint v0.32.3 build syntax and patch sanity checks."""
from pathlib import Path
import json, re, subprocess, shutil, tempfile
ROOT=Path(__file__).resolve().parents[1]
root_source=ROOT/'src/main/java'
java=root_source/'dev/projectflint/recipe/ClayGrinderRecipeGuideRegistry.java'
text=java.read_text()
assert 'new Definition("projectflint:loose_mud_bricks", 1, "projectflint:loose_mud_brick_slab", 2)\n\t);' in text
assert not re.search(r',\s*\);', text), 'trailing comma before List.of closing parenthesis'
assert 'mod_version=0.32.3' in (ROOT/'gradle.properties').read_text()
assert 'APPLY_PROJECTFLINT_0_32_3.bat' in (ROOT/'build.bat').read_text()
assert 'RetiredRecipePolicy.isRetired(' in (root_source/'dev/projectflint/mixin/RecipeManagerMixin.java').read_text()
for file in ROOT.glob('src/main/resources/**/*.json'): json.loads(file.read_text())
# JavaCompiler parser is available on a Java 21 JDK and checks all sources, even
# when the Gradle plugin or dependency classpath is not available offline.
program = r'''import javax.tools.*;
import com.sun.source.util.JavacTask;
import java.nio.file.*;
import java.util.*;
public class ParseAll {
 public static void main(String[] args) throws Exception {
  var compiler=ToolProvider.getSystemJavaCompiler();
  var errors=new DiagnosticCollector<JavaFileObject>();
  var manager=compiler.getStandardFileManager(errors,null,null);
  var files=new ArrayList<java.io.File>();
  try(var paths=Files.walk(Path.of(args[0]))){ paths.filter(x->x.toString().endsWith(".java")).forEach(x->files.add(x.toFile())); }
  var task=(JavacTask)compiler.getTask(null,manager,errors,List.of("--release","21","-proc:none"),null,manager.getJavaFileObjectsFromFiles(files));
  for(var ignored:task.parse()) {}
  var bad=errors.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).toList();
  for(var d:bad)System.err.println(d);
  if(!bad.isEmpty())System.exit(1);
  System.out.println("PASS: "+files.size()+" Java files parsed with no syntax errors");
 }
}
'''
with tempfile.TemporaryDirectory() as td:
    target=Path(td)/'ParseAll.java'
    target.write_text(program)
    subprocess.run(['javac',str(target)],check=True)
    subprocess.run(['java','-cp',td,'ParseAll',str(root_source)],check=True)
print('PASS: v0.32.3 version, Java syntax, JSON and retained server recipe guard')
