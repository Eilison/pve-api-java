package it.corsinvest.proxmoxve.api.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Slf4j
public class HttpUtils {

    public final static String BOUNDARY = "WebKitFormBoundary" + UUID.randomUUID().toString()
            .toLowerCase().replaceAll("-", "").substring(0, 15);// 边界标识
    public final static String PREFIX = "--";// 必须存在
    public final static String LINE_END = "\r\n";

    public static void writeParams(Map<String, Object> requestText,
                                    OutputStream os) throws Exception {
        try{
            String msg = "请求参数部分:\n";
            if (requestText == null || requestText.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, Object>> set = requestText.entrySet();
                Iterator<Map.Entry<String, Object>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"").append(LINE_END);
//                    requestParams.append("Content-Type: text/plain; charset=utf-8")
//                            .append(LINE_END);
//                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
//                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(entry.getValue());
                    requestParams.append(LINE_END);
                }
                os.write(requestParams.toString().getBytes());
                os.flush();

                msg += requestParams.toString();
            }

            System.out.println(msg);
        }catch(Exception e){
            log.error("writeParams failed: {}", e.getCause().getMessage());
            throw new Exception(e);
        }
    }

    public static void writeFile(Map<String, Object> requestFile,
                                 OutputStream os) throws Exception {
        InputStream is = null;
        try{
            String msg = "请求上传文件部分:\n";
            if (requestFile == null || requestFile.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, Object>> set = requestFile.entrySet();
                Iterator<Map.Entry<String, Object>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    Object value = entry.getValue();
                    if (!(value instanceof File)) {
                        continue;
                    }
                    File file = (File) value;
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"; filename=\"")
                            .append(file.getName()).append("\"")
                            .append(LINE_END);
                    requestParams.append("Content-Type:")
                            .append("application/octet-stream")
                            .append(LINE_END);
//                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
//                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容

                        os.write(requestParams.toString().getBytes());

                    is = new FileInputStream(file);

                    byte[] buffer = new byte[1024*1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.write(LINE_END.getBytes());
                    os.flush();

                    msg += requestParams.toString();
                }
            }
            System.out.println(msg);
        }catch(Exception e){
            log.error("writeFile failed", e);
            throw new Exception(e);
        }finally{
            try{
                if(is!=null){
                    is.close();
                }
            }catch(Exception e){
                log.error("writeFile FileInputStream close failed", e);
                throw new Exception(e);
            }
        }
    }
}
