/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativeplatform.toolchain.internal.msvcpp;

import org.gradle.api.Transformer;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.toolchain.internal.ArgsTransformer;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker;
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec;
import org.gradle.nativeplatform.toolchain.internal.NativeCompiler;
import org.gradle.nativeplatform.toolchain.internal.OptionsFileArgsWriter;
import org.gradle.util.GFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class VisualCppNativeCompiler<T extends NativeCompileSpec> extends NativeCompiler<T> {

    VisualCppNativeCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, ArgsTransformer<T> argsTransformer, Transformer<T, T> specTransformer, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
        super(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineToolInvocationWorker, invocationContext, argsTransformer, specTransformer, objectFileExtension, useCommandFile, workerLeaseService);
    }

    @Override
    protected List<String> getOutputArgs(T spec, File outputFile) {
        List<String> args = new ArrayList<String>();
        if (spec.isDebuggable()) {
            args.add("/Fd" + GFileUtils.relativizeToBase(spec.getWorkingDir(), new File(outputFile.getParentFile(), outputFile.getName() + ".pdb")));
        }
        // MSVC doesn't allow a space between Fo and the file name
        args.add("/Fo" + GFileUtils.relativizeToBase(spec.getWorkingDir(), outputFile));
        return args;
    }

    @Override
    protected void addOptionsFileArgs(T spec, List<String> args, File tempDir) {
        OptionsFileArgsWriter writer = new VisualCppOptionsFileArgsWriter(spec.getWorkingDir(), tempDir);
        // modifies args in place
        writer.execute(args);
    }

    @Override
    protected List<String> getPCHArgs(T spec) {
        List<String> pchArgs = new ArrayList<String>();
        // TODO: Check if this needs to be relativized too.
        if (spec.getPreCompiledHeader() != null && spec.getPreCompiledHeaderObjectFile() != null) {
            String lastHeader = spec.getPreCompiledHeader();

            pchArgs.add("/Yu".concat(lastHeader));
            pchArgs.add("/Fp".concat(spec.getPreCompiledHeaderObjectFile().getAbsolutePath()));
        }
        return pchArgs;
    }
}
