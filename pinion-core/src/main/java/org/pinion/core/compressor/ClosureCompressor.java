package org.pinion.core.compressor;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import org.pinion.core.asset.Asset;

public class ClosureCompressor implements Compressor {
  final CompilerOptions options;

  public ClosureCompressor(CompilerOptions options) {
    if (options == null) {
      this.options = new CompilerOptions();
      CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
    } else {
      this.options = options;
    }
  }

  @Override
  public String run(final String input, final Asset asset) {
    final Compiler compiler = new Compiler();
    final JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    final JSSourceFile in = JSSourceFile.fromCode(asset.logicalPath, input);

    compiler.compile(extern, in, options);

    return compiler.toSource();
  }
}
