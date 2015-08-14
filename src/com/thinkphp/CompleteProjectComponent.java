package com.thinkphp;

/**
 * Created by 625305505@qq.com on 2015/6/15.
 */
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.thinkphp.SelfRecursiveElementVisitor;

public class CompleteProjectComponent implements ProjectComponent{
    private Project _project;
    private static final String META_FILE = ".phpstorm.meta.php";
    private static final String THINKPHP = "ThinkPHP";
    private static final String MODEL = "Model";
    private static final String LOGIC = "Logic";
    private static final String SERVICE = "Service";

    public CompleteProjectComponent(Project project) {
        this._project = project;
    }

    @NotNull
    public String getComponentName() {
        return "CompleteProjectComponent";
    }
    public void projectOpened() {
    }
    public void projectClosed() {
    }
    public void disposeComponent() {
    }
    public void initComponent() {
        if(isThinkFrameWorkExist())
        {
            this.initFileListeners();
        }
    }

    private void initFileListeners() {
        VirtualFileManager.getInstance().addVirtualFileListener(new MetaFileListener(this._project));
    }

    private boolean isThinkFrameWorkExist()
    {
        String path = this._project.getBasePath();
        File root = new File(path);
        return findThinkFrameWork(root);
    }

    private  boolean  findThinkFrameWork(File root)
    {
        File[] files = root.listFiles();
         File[] arr$ = files;
        int len$ = files.length;
        for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            if(file.isFile()) {
                continue;
            } else if(file.isDirectory()) {
                if(file.getName().contains(THINKPHP)){
                    return true;
                }else{
                   if(findThinkFrameWork(file))
                   {
                       return true;
                   }
                }
            }
        }
        return false;
    }

    protected static boolean isMetaFileExist(Project project)
    {
        String path = project.getBasePath();
        File root = new File(path);
        return findMetaFile(root);
    }


    protected static boolean  findMetaFile(File root)
    {
        File[] files = root.listFiles();
        File[] arr$ = files;
        int len$ = files.length;
        for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            if(file.isDirectory()) {
                if(findMetaFile(file))
                {
                    return true;
                }
            } else if(file.isFile()) {
                if(file.getName().contains(META_FILE)){
                    return true;
                }else{
                    continue;
                }
            }
        }
        return false;
    }

    protected static void createMetaFile(final Project project) {
        Map models  =  findModels(project);
        Map logics  =  findLogics(project);
        Map services  =  findServices(project);
        if(models.isEmpty()){
            return ;
        }
        String content = "<?php\n\tnamespace PHPSTORM_META {\n\t/** @noinspection PhpUnusedLocalVariableInspection */\n\t/** @noinspection PhpIllegalArrayKeyTypeInspection */\n\t$STATIC_METHOD_TYPES = [\n";
        if(!models.isEmpty()){
            Iterator i$ = models.entrySet().iterator();
            content = content + "\n\t\t\\D(\'\') => [";
            while(i$.hasNext()) {
                Map.Entry entry = (Map.Entry)i$.next();
                String className = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 10));
                String modelName = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 15));
                content = content + "\n\t\t\t'" + modelName + "' instanceof " + (String)entry.getValue() +'\\' +className +",";
            }
            content = content + "\n\t\t],";
        }
        if(!logics.isEmpty()){
            Iterator i$ = logics.entrySet().iterator();
            content = content + "\n\t\t\\DL(\'\') => [";
            while(i$.hasNext()) {
                Map.Entry entry = (Map.Entry)i$.next();
                String className = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 10));
                String modelName = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 15));
                content = content + "\n\t\t\t'" + modelName + "' instanceof " + (String)entry.getValue() +'\\' +className +",";
            }
            content = content + "\n\t\t],";
        }
        if(!services.isEmpty()){
            Iterator i$ = services.entrySet().iterator();
            content = content + "\n\t\t\\DS(\'\') => [";
            while(i$.hasNext()) {
                Map.Entry entry = (Map.Entry)i$.next();
                String className = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 10));
                String modelName = ((String) entry.getKey()).substring(0,(((String) entry.getKey()).length() - 15));
                content = content + "\n\t\t\t'" + modelName + "' instanceof " + (String)entry.getValue() +'\\' +className +",";
            }
            content = content + "\n\t\t]";
        }
        content = content + "\n\t];\n}";
        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(META_FILE, PhpFileType.INSTANCE, content);
        psiDirectory.add(psiFile);
    }


    protected static Map findModels(final Project project)
    {
        File root = new File(project.getBasePath());
        Map models = findModelFiles(root ,project,MODEL);
        return models;
    }
    protected static Map findLogics(final Project project)
    {
        File root = new File(project.getBasePath());
        Map models = findModelFiles(root ,project,LOGIC);
        return models;
    }
    protected static Map findServices(final Project project)
    {
        File root = new File(project.getBasePath());
        Map models = findModelFiles(root ,project,SERVICE);
        return models;
    }
    private static Map  findModelFiles(File root ,final Project project, String type)
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
                if(file.getName().contains(type))
                {
                    File model = file;
                    File[] modelFiles = model.listFiles();
                    File[] Arr$ = modelFiles;
                    int Len$ = modelFiles.length;
                    for(int j$ = 0; j$ < Len$; ++j$) {
                        File normalFile = Arr$[j$];
                        VirtualFile virFile = LocalFileSystem.getInstance().findFileByIoFile(normalFile);
                        final   PsiFile phpfile = PsiManager.getInstance(project).findFile(virFile);
                        //SelfRecursiveElementVisitor
                        phpfile.accept(new SelfRecursiveElementVisitor() {
                            public void visitPhpNamespace(PhpNamespace namespace){
                                if(namespace.getElementType().toString().contains("Namespace"))
                                {
                                    models.put( phpfile.getName(), namespace.getFQN());
                                }
                            }
                        });
                    }
                }else{
                    models.putAll(findModelFiles(file,project,type));
                }
            }
        }
        return models;
    }
}
