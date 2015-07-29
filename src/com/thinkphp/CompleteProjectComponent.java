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
import com.jetbrains.php.lang.psi.PhpFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.PsiElement;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.thinkphp.ThinkphpDFunctionProvider;
public class CompleteProjectComponent implements ProjectComponent{
    private Project _project;
    private static final String META_FILE = ".phpstorm.meta.php";
    private static final String THINKPHP = "ThinkPHP";

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
            if(!isMetaFileExist())
            {
               ThinkphpDFunctionProvider.createMetaFile(this._project);
            }
        }
        this.initFileListeners();
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

    private boolean isMetaFileExist()
    {
        String path = this._project.getBasePath();
        File root = new File(path);
        return findMetaFile(root);
    }

    private  boolean  findMetaFile(File root)
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

}
