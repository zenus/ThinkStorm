package com.thinkphp;
/**
 * Created by 625305505@qq.com on 2015/6/15.
 */

import com.intellij.openapi.project.Project;
import com.thinkphp.SelfStaticFactoryTypeProvider;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
public class MetaFileListener extends VirtualFileAdapter{

    private static final String META_FILE = ".phpstorm.meta.php";
    protected Project _project;
    private boolean lock = false;

    public MetaFileListener(Project project) {
        this._project = project;
    }

    public void fileCreated(VirtualFileEvent event) {
        this.refreshMetaFile(event);
    }

    public void fileDeleted(VirtualFileEvent event) {
        this.refreshMetaFile(event);
    }

    public void contentsChanged(VirtualFileEvent event) {
        if(isModelFile(event)){
            File dir = new  File(this._project.getBasePath());
            File normalFile = findMetaFile(dir);
            if(normalFile == null && !lock)
            {
                lock = true;
                com.thinkphp.CompleteProjectComponent.createMetaFile(this._project);
                lock = false;
            }
        }
    }

    private void refreshMetaFile(VirtualFileEvent event)
    {
        if(isModelFile(event)){
            File dir = new  File(this._project.getBasePath());
            File normalFile = findMetaFile(dir);
            if(normalFile != null)
            {
                VirtualFile  virFile = LocalFileSystem.getInstance().findFileByIoFile(normalFile);
                 PsiManager.getInstance(this._project).findFile(virFile).delete();
               com.thinkphp.CompleteProjectComponent.createMetaFile(this._project);
            }
        }
    }
    protected boolean isModelFile(VirtualFileEvent event) {
        return (event.getFileName().contains("Model.class.php") && event.getParent() != null && event.getParent().getName().contains("Model"))
              || (event.getFileName().contains("Logic.class.php") && event.getParent() != null && event.getParent().getName().contains("Logic"))
              ||(event.getFileName().contains("Service.class.php") && event.getParent() != null && event.getParent().getName().contains("Service"));
    }

    private  File  findMetaFile(File root)
    {
        File[] files = root.listFiles();
        File[] arr$ = files;
        int len$ = files.length;
        for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            if(file.isDirectory()) {
                File find = findMetaFile(file);
                if( find != null)
                {
                    return find;
                }
            } else if(file.isFile()) {
                if(file.getName().contains(META_FILE)){
                    return file;
                }else{
                    continue;
                }
            }
        }
        return null;
    }
}
