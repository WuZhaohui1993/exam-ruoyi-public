package com.ruoyi.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 基于ZXing库实现二维码生成功能
 * 
 * @author ruoyi
 */
public class QrCodeUtil {
    private static final String CHARSET = "UTF-8";
    private static final String FORMAT_NAME = "PNG";

    /**
     * 生成二维码并返回Base64编码的图片字符串
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BitMatrix bitMatrix = createBitMatrix(content, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, FORMAT_NAME, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 生成二维码并保存到文件
     *
     * @param content  二维码内容
     * @param filePath 文件路径
     * @param width    宽度
     * @param height   高度
     */
    public static void generateQrCodeToFile(String content, String filePath, int width, int height) {
        try {
            BitMatrix bitMatrix = createBitMatrix(content, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, FORMAT_NAME, path);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码文件失败", e);
        }
    }

    /**
     * 创建BitMatrix
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return BitMatrix
     */
    private static BitMatrix createBitMatrix(String content, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
    }

    /**
     * 生成考试二维码URL内容
     *
     * @param baseUrl 基础URL（如：http://localhost:1024）
     * @param examId  考试ID
     * @return 完整的H5考试URL
     */
    public static String generateExamQrCodeContent(String baseUrl, Long examId) {
        return baseUrl + "/h5/exam/" + examId;
    }

    /**
     * 生成考试二维码相对URL路径
     *
     * @param examId 考试ID
     * @return 相对H5考试URL路径
     */
    public static String generateExamQrCodePath(Long examId) {
        return "/h5/exam/" + examId;
    }
}
