package com.reggie.controller;

import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * file upload and download
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * file upload
     * @param file
     * @return
     */

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //file is a temp file, need to transfer to another location, otherwise it will be deleted after this editing
        log.info(file.toString());

        //original file name
        //String originalFilename = file.getOriginalFilename();

        //get format from origianl file
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //use UUID to avoid same file name
        String filename = UUID.randomUUID().toString() + suffix;

        //create a folder
        File dir = new File(basePath);
        //if current folder exist
        if(!dir.exists()){
            //create a new folder
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath + filename));
        }catch (IOException e){
            e.printStackTrace();
        }

        return R.success(filename);
    }

    /**
     * file download
     * @param response
     * @param name
     */
    @GetMapping("/download")
    public void download( HttpServletResponse response, String name){
        //input stream to read file name
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //output stream to write file into web and display the picture
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //close streams
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
