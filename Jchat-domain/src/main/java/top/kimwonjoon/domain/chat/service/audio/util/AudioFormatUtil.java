package top.kimwonjoon.domain.chat.service.audio.util;

/**
 * @ClassName AudioFormatUtil
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/9/23 09:53
 */

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class AudioFormatUtil {

    private static final Logger logger = LoggerFactory.getLogger(AudioFormatUtil.class);

    /**
     * 将Base64音频数据转换为指定格式
     */
    public static byte[] convertAudioFormat(String base64AudioData, String inputFormat, String outputFormat) {
        try {
            // 解码Base64数据
            byte[] audioBytes = Base64.getDecoder().decode(base64AudioData);

            // 根据输入和输出格式选择转换方法
            switch (outputFormat.toLowerCase()) {
                case "wav":
                    return convertToWav(audioBytes, inputFormat);
                case "mp3":
                    return convertToMp3(audioBytes, inputFormat);
                case "webm":
                    return convertToWebm(audioBytes, inputFormat);
                case "ogg":
                    return convertToOgg(audioBytes, inputFormat);
                default:
                    logger.warn("不支持的输出格式: {}", outputFormat);
                    return audioBytes; // 返回原始数据
            }

        } catch (Exception e) {
            logger.error("音频格式转换失败", e);
            throw new RuntimeException("音频格式转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换为WAV格式
     */
    private static byte[] convertToWav(byte[] audioBytes, String inputFormat) throws Exception {
        if ("wav".equalsIgnoreCase(inputFormat)) {
            return audioBytes; // 已经是WAV格式
        }

        // 创建临时文件
        Path tempInputFile = createTempFile(audioBytes, inputFormat);
        Path tempOutputFile = Files.createTempFile("audio_output", ".wav");

        try {
            // 使用Java Sound API进行转换
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempInputFile.toFile());
            AudioFormat format = audioInputStream.getFormat();

            // 创建WAV格式
            AudioFormat wavFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16, // 16位
                    format.getChannels(),
                    format.getChannels() * 2, // frame size
                    format.getSampleRate(),
                    false // little endian
            );

            // 转换音频流
            AudioInputStream wavStream = AudioSystem.getAudioInputStream(wavFormat, audioInputStream);

            // 写入WAV文件
            AudioSystem.write(wavStream, AudioFileFormat.Type.WAVE, tempOutputFile.toFile());

            // 读取转换后的文件
            return Files.readAllBytes(tempOutputFile);

        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempInputFile);
            Files.deleteIfExists(tempOutputFile);
        }
    }

    /**
     * 转换为MP3格式 (需要外部工具如FFmpeg)
     */
    private static byte[] convertToMp3(byte[] audioBytes, String inputFormat) throws Exception {
        return convertWithFFmpeg(audioBytes, inputFormat, "mp3");
    }

    /**
     * 转换为WebM格式
     */
    private static byte[] convertToWebm(byte[] audioBytes, String inputFormat) throws Exception {
        if ("webm".equalsIgnoreCase(inputFormat)) {
            return audioBytes; // 已经是WebM格式
        }
        return convertWithFFmpeg(audioBytes, inputFormat, "webm");
    }

    /**
     * 转换为OGG格式
     */
    private static byte[] convertToOgg(byte[] audioBytes, String inputFormat) throws Exception {
        return convertWithFFmpeg(audioBytes, inputFormat, "ogg");
    }

    /**
     * 使用FFmpeg进行音频格式转换
     */
    private static byte[] convertWithFFmpeg(byte[] audioBytes, String inputFormat, String outputFormat) throws Exception {
        // 创建临时文件
        Path tempInputFile = createTempFile(audioBytes, inputFormat);
        Path tempOutputFile = Files.createTempFile("audio_output", "." + outputFormat);

        try {
            // 构建FFmpeg命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", tempInputFile.toString(),
                    "-y", // 覆盖输出文件
                    "-acodec", getCodecForFormat(outputFormat),
                    "-ar", "16000", // 采样率
                    "-ac", "1", // 单声道
                    tempOutputFile.toString()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取输出
            String output = IOUtils.toString(process.getInputStream(), "UTF-8");
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.error("FFmpeg转换失败，退出码: {}, 输出: {}", exitCode, output);
                throw new RuntimeException("FFmpeg转换失败: " + output);
            }

            // 读取转换后的文件
            return Files.readAllBytes(tempOutputFile);

        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempInputFile);
            Files.deleteIfExists(tempOutputFile);
        }
    }

    /**
     * 获取格式对应的编解码器
     */
    private static String getCodecForFormat(String format) {
        switch (format.toLowerCase()) {
            case "mp3":
                return "libmp3lame";
            case "webm":
                return "libopus";
            case "ogg":
                return "libvorbis";
            case "wav":
                return "pcm_s16le";
            default:
                return "copy";
        }
    }

    /**
     * 创建临时文件
     */
    private static Path createTempFile(byte[] data, String extension) throws IOException {
        Path tempFile = Files.createTempFile("audio_input", "." + extension);
        Files.write(tempFile, data);
        return tempFile;
    }

    /**
     * 获取音频文件信息
     */
    public static AudioInfo getAudioInfo(byte[] audioBytes, String format) {
        try {
            Path tempFile = createTempFile(audioBytes, format);

            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempFile.toFile());
                AudioFormat audioFormat = audioInputStream.getFormat();

                long frameLength = audioInputStream.getFrameLength();
                double durationInSeconds = (frameLength + 0.0) / audioFormat.getFrameRate();

                return new AudioInfo(
                        audioFormat.getSampleRate(),
                        audioFormat.getChannels(),
                        audioFormat.getSampleSizeInBits(),
                        durationInSeconds,
                        audioBytes.length
                );

            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            logger.error("获取音频信息失败", e);
            return new AudioInfo(0, 0, 0, 0, audioBytes.length);
        }
    }

    /**
     * 音频信息类
     */
    public static class AudioInfo {
        private final float sampleRate;
        private final int channels;
        private final int bitDepth;
        private final double duration;
        private final int fileSize;

        public AudioInfo(float sampleRate, int channels, int bitDepth, double duration, int fileSize) {
            this.sampleRate = sampleRate;
            this.channels = channels;
            this.bitDepth = bitDepth;
            this.duration = duration;
            this.fileSize = fileSize;
        }

        // Getters
        public float getSampleRate() { return sampleRate; }
        public int getChannels() { return channels; }
        public int getBitDepth() { return bitDepth; }
        public double getDuration() { return duration; }
        public int getFileSize() { return fileSize; }

        @Override
        public String toString() {
            return String.format("AudioInfo{sampleRate=%.1f, channels=%d, bitDepth=%d, duration=%.2fs, fileSize=%d bytes}",
                    sampleRate, channels, bitDepth, duration, fileSize);
        }
    }
}