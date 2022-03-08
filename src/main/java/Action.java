
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author minzx
 * @data 2022/3/7
 */
public class Action {
    final static String [] IMAGE_TYPE_ARR= {"jpg", "png"};

    static String ROOT_SOURCE = "";

    public static void main(String[] args) {
        ROOT_SOURCE = "C:\\Users\\xx\\Desktop\\testImage";
        retrieval();
    }


    private static void retrieval(){
        File rootFile = new File(ROOT_SOURCE);
        //遍历image文件
        File[] fs = rootFile.listFiles();
        //新建文件夹名字
        List<Image> imageList = new ArrayList<>();
        for(File file:fs){
            if(!file.isDirectory()){
                System.out.println("判断文件格式");
                String fileType= FileUtil.getSuffix(file);
                for(String imageType:IMAGE_TYPE_ARR){
                    if(imageType.equalsIgnoreCase(fileType)){
                        System.out.println("符合条件");
                        Image image = new Image();
                        image.setName(FileUtil.getPrefix(file));
                        image.setType(fileType);
                        imageList.add(image);
                        break;
                    }
                }
            }
        }
        //创建文件夹
        if(createFolder(imageList)){
            //移动文件
            moveFile(imageList);
        }
    }

    private static void moveFile(List<Image> imageList) {
        System.out.println("move file ........");
        threadMoveFile(imageList);
    }


    private static boolean createFolder(List<Image> imageList) {
        System.out.println("do create folder ...");
        if(CollectionUtil.isEmpty(imageList)){
            System.out.println("no folder need to be create.......");
            return false;
        }
        //
        threadCreateFolder(imageList);
        return true;
    }


    private static void threadMoveFile(List<Image> imageList){
        System.out.println("do threadMoveFile ...");
        for(Image image:imageList){
            String fileName = image.getName()+"."+image.getType();
            FileUtil.move(FileUtil.file(ROOT_SOURCE, fileName), FileUtil.file(ROOT_SOURCE + File.separator + image.getName(), fileName),
                    true);
        }

    }

    private static void threadCreateFolder(List<Image> imageList){
        System.out.println("do threadCreateFolder ...");
        ExecutorService executor = new ThreadPoolExecutor(2, 2, 2,
                TimeUnit.MINUTES, new ArrayBlockingQueue(10));
        for(Image image:imageList){
            CompletableFuture future = CompletableFuture.supplyAsync(
                ()->{
                    //业务操作
                    System.out.println("业务操作: folder name:" + image.getName());
                    File file = new File(ROOT_SOURCE + File.separator + image.getName());
                    if(!file.isDirectory()){
                        file.mkdir();
                    }
                    return "success";
                }, executor).exceptionally(e ->{
                    System.out.println(image.getName());
                    System.out.println(e);
                    return "false";
            });
        }
        //关闭线程流
        executor.shutdown();

        //等待全部任务结束返回结果
        while (!executor.isTerminated()) {
            System.out.println("no terminated");
            try {
                System.out.println("我要休眠一下");
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
