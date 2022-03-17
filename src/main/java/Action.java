
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    static String [] IMAGE_TYPE_ARR;

    static String ROOT_SOURCE = "";

    public static void main(String[] args) {
        JFrame jf = new JFrame("图片分类");
        jf.setSize(400, 300);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPane=new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = null;

        contentPane.setLayout(gridBagLayout);
        jf.setContentPane(contentPane);

        JPanel pane1=new JPanel();
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridBagLayout.addLayoutComponent(pane1, c);
        contentPane.add(pane1);

        JPanel pane2=new JPanel();
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridBagLayout.addLayoutComponent(pane2, c);
        contentPane.add(pane2);

        JPanel pane3=new JPanel();
        contentPane.add(pane3);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridBagLayout.addLayoutComponent(pane3, c);

        JPanel pane4=new JPanel();
        contentPane.add(pane4);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridBagLayout.addLayoutComponent(pane4, c);

        JPanel pane5=new JPanel();
        contentPane.add(pane5);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridBagLayout.addLayoutComponent(pane5, c);

        JLabel label1=new JLabel("图片格式(多个使用,分割)：");
        pane1.add(label1);

        JTextField textField1=new JTextField();
        textField1.setColumns(10);
        pane2.add(textField1);

        JLabel label2=new JLabel("图片目录：");
        pane3.add(label2);
        JTextField textField2=new JTextField();
        textField2.setColumns(10);
        pane4.add(textField2);



        // 创建一个按钮
        final JButton btn = new JButton("Start");
        // 添加按钮的点击事件监听器
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("按钮被点击");
                System.out.println(textField1.getText());
                System.out.println(textField2.getText());

                IMAGE_TYPE_ARR = textField1.getText().split(",");
                ROOT_SOURCE = textField2.getText();
                if(retrieval()){
                    JOptionPane.showMessageDialog(new JPanel(),"执行结束", "标题",JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        pane5.add(btn);
        jf.setVisible(true);
    }


    private static boolean retrieval(){
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
        return true;
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
        /*ExecutorService executor = new ThreadPoolExecutor(2, 2 , 2,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(10));
        CompletableFuture future = CompletableFuture.supplyAsync(
            ()-> {
                System.out.println("");


                return "true";
            }
        , executor).exceptionally(e-> {
            System.out.println("");


            return "false";
        });
        future.join()*/
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
            future.join();
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
