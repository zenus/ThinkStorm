package com.thinkphp;

/**
 * Created by 625305505@qq.com on 2015/6/15.
 */

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern.Capture;
import com.intellij.psi.PsiElement;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.elements.PhpNamespaceReference;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.ParameterListOwner;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import com.jetbrains.php.lang.psi.resolve.types.PhpStaticFactoryTypeProvider;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import gnu.trove.THashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class ThinkphpDFunctionProvider extends PhpStaticFactoryTypeProvider {

    private static final String META_FILE = ".phpstorm.meta.php";
    private static final String MODEL = "Model";

    public ThinkphpDFunctionProvider() {
    }

    public static PsiFile[] getMetaFiles(final Project project) {
         return FilenameIndex.getFilesByName(project, META_FILE, GlobalSearchScope.allScope(project));
    }


    protected static void createMetaFile(final Project project) {
        Map models  =  findModels(project);
        if(models.isEmpty()){
            return ;
        }
        String content = "<?php\n\tnamespace PHPSTORM_META {\n\t/** @noinspection PhpUnusedLocalVariableInspection */\n\t/** @noinspection PhpIllegalArrayKeyTypeInspection */\n\t$STATIC_METHOD_TYPES = [\n";
        Iterator i$ = models.entrySet().iterator();
        content = content + "\n\t\t\\D(\'\') => [";
        while(i$.hasNext()) {
           Map.Entry entry = (Map.Entry)i$.next();
           String className = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 10));
            String modelName = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 15));
        content = content + "\n\t\t\t'" + modelName + "' instanceof " + (String)entry.getValue() +'\\' +className +",";
                }
        content = content + "\n\t\t]";
        content = content + "\n\t];\n}";
        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(META_FILE, PhpFileType.INSTANCE, content);
        psiDirectory.add(psiFile);
    }


    protected static Map findModels(final Project project)
    {
        File root = new File(project.getBasePath());
        Map models = findModelFiles(root ,project);
        return models;
    }
    private static Map  findModelFiles(File root ,final Project project)
    {
      final  Map models = new HashMap();
        File[] files = root.listFiles();
        File[] arr$ = files;
        int len$ = files.length;
        for(int i$ = 0; i$ < len$; ++i$)
        {
            File file = arr$[i$];
            if(file.isFile())
            {
                continue;
            } else if(file.isDirectory()) {
                if(file.getName().contains(MODEL))
                {
                    File model = file;
                    File[] modelFiles = model.listFiles();
                    File[] Arr$ = modelFiles;
                    int Len$ = modelFiles.length;
                    for(int j$ = 0; j$ < Len$; ++j$) {
                        File normalFile = Arr$[j$];
                      VirtualFile  virFile = LocalFileSystem.getInstance().findFileByIoFile(normalFile);
                        final   PsiFile phpfile = PsiManager.getInstance(project).findFile(virFile);
                        if(phpfile instanceof PhpFile) {
                            phpfile.accept(new PhpRecursiveElementVisitor() {
                                public void visitPhpNamespace(PhpNamespace namespace){
                                    if(namespace.getElementType().toString().contains("Namespace"))
                                    {
                                        models.put( phpfile.getName(), namespace.getFQN());
                                    }
                                }
                            });}
                        }
                    }else{
                         models.putAll(findModelFiles(file,project));
                    }
                }
            }
        return models;
        }
    }



