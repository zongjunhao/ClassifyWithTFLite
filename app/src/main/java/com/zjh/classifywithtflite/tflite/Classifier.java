package com.zjh.classifywithtflite.tflite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import com.zjh.classifywithtflite.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public abstract class Classifier {
    private static final String TAG = "Classifier";

    private static final int MAX_RESULTS = 3; // 展示在界面上的结果的数量
    private MappedByteBuffer tfliteModel; // 加载的TensorFlow Lite模型
    private final int imageSizeX; // 图像在x轴方向的长度
    private final int imageSizeY; // 图像在y轴方向的长度
    protected Interpreter tflite; // 使用Tensorflow Lite运行模型推理的驱动程序类的实例。
    private final Interpreter.Options tfliteOptions = new Interpreter.Options(); //配置interpreter的选项
    private List<String> labels; // 输出标签
    private TensorImage inputImageBuffer; // 输入图像的TensorBuffer
    private final TensorBuffer outputProbabilityBuffer; // 输出probability TensorBuffer
    private final TensorProcessor probabilityProcessor; // 用于处理输出probability的Processer

    /**
     * 创建一个classifier（分类器
     *
     * @param activity 当前activity
     * @return classifier
     */
    public static Classifier create(Activity activity) throws IOException {
        return new ClassifierFloatMobileNet(activity);
    }

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     * Classifier返回的描述识别到的不变的结果
     */
    public static class Recognition {

        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         * 已识别内容的唯一标识符。特定于类，而不是对象的实例。
         */
        private final String id;

        /**
         * 显示识别结果的名字
         */
        private final String title;

        /**
         * 识别结果的得分，越高越好
         */
        private final Float confidence;

        /**
         * Optional location within the source image for the location of the recognized object.
         * 在源图像中可选的位置，用于识别对象的位置。
         */
        private RectF location;

        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    /**
     * Initializes a {@code Classifier}.
     * 初始化一个Classifier
     */
    protected Classifier(Activity activity) throws IOException {
        tfliteModel = FileUtil.loadMappedFile(activity, getModelPath()); // 从模型文件中加载模型
        tfliteOptions.setNumThreads(1);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        // 从标签文件中加载标签
        labels = FileUtil.loadLabels(activity, getLabelPath());

        // Reads type and shape of input and output tensors, respectively.
        // 分别读取输入张量和输出张量的类型和形状
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        // 创建输入tensor（张量）
        inputImageBuffer = new TensorImage(imageDataType);

        // 创建输出tensor和它的processor
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        // Creates the post processor for the output probability.
        // 为输出概率创建后处理器。
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        Log.d(TAG, "Classifier: Created a TensorFlow Lote Image Classifier");
    }

    /**
     * Runs inference and returns the classification results.
     * 运行推理并返回分类结果。
     */
    public List<Recognition> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
        // 把这个方法记录下来，以便用系统分析。
        Trace.beginSection("recognizeImage");// 开始识别图片

        Trace.beginSection("loadImage");// 开始加载图片
        long startTimeForLoadImage = SystemClock.uptimeMillis();
        inputImageBuffer = loadImage(bitmap, sensorOrientation);// 加载图片
        long endTimeForLoadImage = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.d(TAG, "Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage));//输出加载时间

        // Runs the inference call. 进行推理调用
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();
        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.d(TAG, "recognizeImage: Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

        // Gets the map of label and probability
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                .getMapWithFloatValue();
        Trace.endSection();

        // Gets top-k results
        return getTopKProbability(labeledProbability);
    }

    /**
     * 获取图像在x轴方向的长度
     */
    public int getImageSizeX() {
        return imageSizeX;
    }

    /**
     * 获取图像在y轴方向的长度
     */
    public int getImageSizeY() {
        return imageSizeY;
    }

    /**
     * 加载输入的图像，并应用于预处理
     */
    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        // 将bitmap加载进一个TensorImage
        inputImageBuffer.load(bitmap);

        // 为TensorImage创建processor
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int numRoration = sensorOrientation / 90;
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
                .add(new Rot90Op(numRoration))
                .add(getPreprocessNormalizeOp())
                .build();
        return imageProcessor.process(inputImageBuffer);
    }

    /**
     * 获取排名最高的三个结果
     */
    private static List<Recognition> getTopKProbability(Map<String, Float> labelProb) {
        // 找出最好的识别结果
        // 新建排序队列
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition o1, Recognition o2) {
                                // 人为颠倒来将最有信心的结果放在队列的头部
                                return Float.compare(o2.getConfidence(), o1.getConfidence());
                            }
                        });
        // 将所有的识别结果加入到队列中
        for (Map.Entry<String, Float> entry : labelProb.entrySet()) {
            pq.add(new Recognition("" + entry.getKey(), entry.getKey(), entry.getValue(), null));
        }

        // 将队列中的前3个（或更少）加入到列表中
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionSize; i++) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    /**
     * 获取存储在Assets中的模型文件的名称
     */
    protected abstract String getModelPath();

    /**
     * 获取存储在Assets中的标签文件的名称
     */
    protected abstract String getLabelPath();

    /**
     * Gets the TensorOperator to nomalize the input image in preprocessing.
     * 获取TensorOperator，用于对预处理中的输入图像进行命名。
     */
    protected abstract TensorOperator getPreprocessNormalizeOp();

    /**
     * Gets the TensorOperator to dequantize the output probability in post processing.
     * 获取TensorOperator，用于对后处理中的输出概率进行去量化。
     *
     * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
     * essentially linear transformation). For float model, de-quantize is not required. But to
     * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
     * 1.0f, respectively.
     * 对于量化模型，我们需要使用NormalizeOp对预测进行去量化(因为它们本质上都是线性变换)。
     * 对于浮动模型，不需要去量化。
     * 但为了统一API，浮动模型也加入了去量化。平均值和std分别设置为0.0f和1.0f。
     */
    protected abstract TensorOperator getPostprocessNormalizeOp();
}
