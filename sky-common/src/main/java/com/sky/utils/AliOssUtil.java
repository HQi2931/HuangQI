package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName 原始文件名
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName) {

        // 获取当前系统日期的字符串，格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        // 生成一个新的不重复的文件名
        String extension = objectName.substring(objectName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;
        String finalObjectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, finalObjectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS服务异常: {}", oe.getErrorMessage(), oe);
            throw new RuntimeException("文件上传失败: " + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("客户端异常: {}", ce.getMessage(), ce);
            throw new RuntimeException("文件上传失败: " + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 文件访问路径规则 https://BucketName.Endpoint/ObjectName
        // 从 endpoint 中提取协议和域名，例如: https://oss-cn-beijing.aliyuncs.com
        String[] parts = endpoint.split("//");
        String protocol = parts[0]; // https:
        String domain = parts[1];   // oss-cn-beijing.aliyuncs.com
        
        String url = protocol + "//" + bucketName + "." + domain + "/" + finalObjectName;

        log.info("文件上传到:{}", url);

        return url;
    }
}
