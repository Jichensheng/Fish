package com.example.jcs.fish;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by Jcs on 2017/7/10.
 */

public class FishDrawable extends Drawable {
	private static final String TAG = "Jcs_Fishsss";
	private static final float HEAD_RADIUS = 50;
	protected static final float BODY_LENGHT = HEAD_RADIUS * 3.2f; //第一节身体长度
	private static final int BODY_ALPHA = 220;
	private static final int OTHER_ALPHA = 160;
	private static final int FINS_ALPHA = 100;
	private static final int FINS_LEFT = 1;//左鱼鳍
	private static final int FINS_RIGHT = -1;
	private static final float FINS_LENGTH = HEAD_RADIUS * 1.3f;
	public static final float TOTAL_LENGTH = 6.79f * HEAD_RADIUS;

	private Paint mPaint;
	//控制区域
	private int currentValue = 0;//全局控制标志
	private float mainAngle = 90;//角度表示的角
	protected ObjectAnimator finsAnimator;
	private float waveFrequence = 1;
	//鱼头点
	private PointF headPoint;
	//转弯更自然的中心点
	private PointF middlePoint;
	private float finsAngle = 0;
	private Paint bodyPaint;
	private Path mPath;

	public FishDrawable() {
		init();
	}

	private void init() {

		//路径
		mPath = new Path();
		//画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setDither(true);//防抖
		mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));
		//身体画笔
		bodyPaint = new Paint();
		bodyPaint.setAntiAlias(true);
		bodyPaint.setStyle(Paint.Style.FILL);
		bodyPaint.setDither(true);//防抖
		bodyPaint.setColor(Color.argb(OTHER_ALPHA + 5, 244, 92, 71));
		middlePoint = new PointF(4.18f * HEAD_RADIUS, 4.18f * HEAD_RADIUS);

		//鱼鳍灵动动画
		finsAnimator = ObjectAnimator.ofFloat(this, "finsAngle", 0f, 1f, 0f);
		finsAnimator.setRepeatMode(ValueAnimator.REVERSE);
		finsAnimator.setRepeatCount(new Random().nextInt(3));

		//引擎部分
		ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 540 * 100);
		valueAnimator.setDuration(180 * 1000);
		valueAnimator.setInterpolator(new LinearInterpolator());
		valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
		valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				currentValue = (int) (animation.getAnimatedValue());
				invalidateSelf();
			}
		});
		valueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationRepeat(Animator animation) {
				super.onAnimationRepeat(animation);
				finsAnimator.start();
			}
		});
		valueAnimator.start();

	}

	public PointF getHeadPoint() {
		return headPoint;
	}

	/**
	 * 设置身体主轴线方向角度
	 *
	 * @param mainAngle
	 */
	public void setMainAngle(float mainAngle) {
		this.mainAngle = mainAngle;
	}

	/**
	 * 获取当前角度
	 *
	 * @return
	 */
	public float getMainAngle() {
		return mainAngle;
	}

	/**
	 * 设置头的位置
	 *
	 * @param headPoint
	 */
	public void setHeadPoint(PointF headPoint) {
		this.headPoint = headPoint;
	}

	public ObjectAnimator getFinsAnimator() {
		return finsAnimator;
	}

	public void setMiddlePoint(PointF middlePoint) {
		this.middlePoint = middlePoint;
	}

	public PointF getMiddlePoint() {
		return middlePoint;
	}

	public static float getTotalLength() {
		return TOTAL_LENGTH;
	}

	@Override
	public void draw(Canvas canvas) {

		//生成一个半透明图层，否则与背景白色形成干扰,尺寸必须与view的大小一致否则鱼显示不全
		canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 240, Canvas.ALL_SAVE_FLAG);
		makeBody(canvas, HEAD_RADIUS);
		canvas.restore();
		mPath.reset();
		mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));
	}

	//画身子

	/**
	 * 主方向是头到尾的方向跟X轴正方向的夹角（顺时针为正）
	 * 前进方向和主方向相差180度
	 * R + 3.2R
	 *
	 * @param canvas
	 * @param headRadius
	 */
	private void makeBody(Canvas canvas, float headRadius) {
		//sin参数为弧度值
		//现有角度=原始角度+ sin（域值[-1，1]）*可摆动的角度   sin作用是控制周期摆动
		float angle = mainAngle + (float) Math.sin(Math.toRadians(currentValue * 1.2 * waveFrequence)) * 2;//中心轴线加偏移量和X轴顺时针方向夹角
		headPoint = calculatPoint(middlePoint, BODY_LENGHT / 2,mainAngle);
		//画头
		canvas.drawCircle(headPoint.x, headPoint.y, HEAD_RADIUS, mPaint);
		//右鳍 起点
		PointF pointFinsRight = calculatPoint(headPoint, headRadius * 0.9f, angle -110);
		makeFins(canvas, pointFinsRight, FINS_RIGHT, angle);
		//左鳍 起点
		PointF pointFinsLeft = calculatPoint(headPoint, headRadius * 0.9f, angle +110);
		makeFins(canvas, pointFinsLeft, FINS_LEFT, angle);

		PointF endPoint = calculatPoint(headPoint, BODY_LENGHT, angle-180);
		//躯干2
		PointF mainPoint = new PointF(endPoint.x, endPoint.y);
		makeSegments(canvas, mainPoint, headRadius * 0.7f, 0.6f, angle);
		PointF point1, point2, point3, point4, contralLeft, contralRight;
		//point1和4的初始角度决定发髻线的高低值越大越低
		point1 = calculatPoint(headPoint, headRadius,  angle-80);
		point2 = calculatPoint(endPoint, headRadius * 0.7f, angle-90);
		point3 = calculatPoint(endPoint, headRadius * 0.7f, angle +90);
		point4 = calculatPoint(headPoint, headRadius, angle +80);
		//决定胖瘦
		contralLeft = calculatPoint(headPoint, BODY_LENGHT * 0.56f, angle -130);
		contralRight = calculatPoint(headPoint, BODY_LENGHT * 0.56f, angle +130);
		mPath.reset();
		mPath.moveTo(point1.x, point1.y);
		mPath.quadTo(contralLeft.x, contralLeft.y, point2.x, point2.y);
		mPath.lineTo(point3.x, point3.y);
		mPath.quadTo(contralRight.x, contralRight.y, point4.x, point4.y);
		mPath.lineTo(point1.x, point1.y);

		mPaint.setColor(Color.argb(BODY_ALPHA, 244, 92, 71));
		//画最大的身子
		canvas.drawPath(mPath, mPaint);
	}

	/**
	 * 第二节节肢
	 * 0.7R * 0.6 =1.12R
	 *
	 * @param canvas
	 * @param mainPoint
	 * @param segmentRadius
	 * @param MP            梯形上边下边长度比
	 */
	private void makeSegments(Canvas canvas, PointF mainPoint, float segmentRadius, float MP, float fatherAngle) {
		float angle = fatherAngle + (float) Math.cos(Math.toRadians(currentValue * 1.5 * waveFrequence)) * 15;//中心轴线和X轴顺时针方向夹角
		//身长
		float segementLenght = segmentRadius * (MP + 1);
		PointF endPoint = calculatPoint(mainPoint, segementLenght, angle-180);

		PointF point1, point2, point3, point4;
		point1 = calculatPoint(mainPoint, segmentRadius, angle-90);
		point2 = calculatPoint(endPoint, segmentRadius * MP,angle-90);
		point3 = calculatPoint(endPoint, segmentRadius * MP, angle +90);
		point4 = calculatPoint(mainPoint, segmentRadius, angle+90);

		canvas.drawCircle(mainPoint.x, mainPoint.y, segmentRadius, mPaint);
		canvas.drawCircle(endPoint.x, endPoint.y, segmentRadius * MP, mPaint);
		mPath.reset();
		mPath.moveTo(point1.x, point1.y);
		mPath.lineTo(point2.x, point2.y);
		mPath.lineTo(point3.x, point3.y);
		mPath.lineTo(point4.x, point4.y);
		canvas.drawPath(mPath, mPaint);

		//躯干2
		PointF mainPoint2 = new PointF(endPoint.x, endPoint.y);
		makeSegmentsLong(canvas, mainPoint2, segmentRadius * 0.6f, 0.4f, angle);
	}

	/**
	 * 第三节节肢
	 * 0.7R * 0.6 * (0.4 + 2.7) + 0.7R * 0.6 * 0.4=1.302R + 0.168R
	 *
	 * @param canvas
	 * @param mainPoint
	 * @param segmentRadius
	 * @param MP            梯形上边下边长度比
	 */
	private void makeSegmentsLong(Canvas canvas, PointF mainPoint, float segmentRadius, float MP, float fatherAngle) {
		float angle = fatherAngle + (float) Math.sin(Math.toRadians(currentValue * 1.5 * waveFrequence)) * 35;//中心轴线和X轴顺时针方向夹角
		//身长
		float segementLenght = segmentRadius * (MP + 2.7f);
		PointF endPoint = calculatPoint(mainPoint, segementLenght, angle-180);

		PointF point1, point2, point3, point4;
		point1 = calculatPoint(mainPoint, segmentRadius, angle -90 );
		point2 = calculatPoint(endPoint, segmentRadius * MP,angle -90 );
		point3 = calculatPoint(endPoint, segmentRadius * MP, angle +90);
		point4 = calculatPoint(mainPoint, segmentRadius, angle +90);

		makeTail(canvas, mainPoint, segementLenght, segmentRadius, angle);


		canvas.drawCircle(endPoint.x, endPoint.y, segmentRadius * MP, mPaint);
		mPath.reset();
		mPath.moveTo(point1.x, point1.y);
		mPath.lineTo(point2.x, point2.y);
		mPath.lineTo(point3.x, point3.y);
		mPath.lineTo(point4.x, point4.y);
		canvas.drawPath(mPath, mPaint);
	}

	/**
	 * 鱼鳍
	 *
	 * @param canvas
	 * @param startPoint
	 * @param type
	 */
	private void makeFins(Canvas canvas, PointF startPoint, int type, float fatherAngle) {
		float contralAngle = 115;//鱼鳍三角控制角度
		mPath.reset();
		mPath.moveTo(startPoint.x, startPoint.y);
		PointF endPoint = calculatPoint(startPoint, FINS_LENGTH, type == FINS_RIGHT ? fatherAngle - finsAngle-180 : fatherAngle + finsAngle+180);
		PointF contralPoint = calculatPoint(startPoint, FINS_LENGTH * 1.8f, type == FINS_RIGHT ?
				fatherAngle - contralAngle - finsAngle : fatherAngle + contralAngle + finsAngle);
		mPath.quadTo(contralPoint.x, contralPoint.y, endPoint.x, endPoint.y);
		mPath.lineTo(startPoint.x, startPoint.y);
		mPaint.setColor(Color.argb(FINS_ALPHA, 244, 92, 71));
		canvas.drawPath(mPath, mPaint);
		mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));

	}

	/**
	 * 鱼尾及鱼尾张合
	 *
	 * @param canvas
	 * @param mainPoint
	 * @param length
	 * @param maxWidth
	 */
	private void makeTail(Canvas canvas, PointF mainPoint, float length, float maxWidth, float angle) {
		float newWidth = (float) Math.abs(Math.sin(Math.toRadians(currentValue * 1.7 * waveFrequence)) * maxWidth + HEAD_RADIUS/5*3);
		//endPoint为三角形底边中点
		PointF endPoint = calculatPoint(mainPoint, length, angle-180);
		PointF endPoint2 = calculatPoint(mainPoint, length - 10, angle-180);
		PointF point1, point2, point3, point4;
		point1 = calculatPoint(endPoint, newWidth, angle-90);
		point2 = calculatPoint(endPoint, newWidth, angle +90);
		point3 = calculatPoint(endPoint2, newWidth - 20, angle-90);
		point4 = calculatPoint(endPoint2, newWidth - 20, angle +90);
		//内
		mPath.reset();
		mPath.moveTo(mainPoint.x, mainPoint.y);
		mPath.lineTo(point3.x, point3.y);
		mPath.lineTo(point4.x, point4.y);
		mPath.lineTo(mainPoint.x, mainPoint.y);
		canvas.drawPath(mPath, mPaint);
		//外
		mPath.reset();
		mPath.moveTo(mainPoint.x, mainPoint.y);
		mPath.lineTo(point1.x, point1.y);
		mPath.lineTo(point2.x, point2.y);
		mPath.lineTo(mainPoint.x, mainPoint.y);
		canvas.drawPath(mPath, mPaint);

	}

	private void setFinsAngle(float currentValue) {
		finsAngle = 45 * currentValue;
	}


	public void setWaveFrequence(float waveFrequence) {
		this.waveFrequence = waveFrequence;
	}

	/**
	 *  输入起点、长度、旋转角度计算终点
	 * @param startPoint 起点
	 * @param length 长度
	 * @param angle 旋转角度
	 * @return 计算结果点
	 */
	private static PointF calculatPoint(PointF startPoint, float length, float angle) {
		float deltaX = (float) Math.cos(Math.toRadians(angle)) * length;
        //符合Android坐标的y轴朝下的标准
		float deltaY = (float) Math.sin(Math.toRadians(angle-180)) * length;
		return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);
	}


	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		mPaint.setColorFilter(colorFilter);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/**
	 * 高度要容得下两个鱼身长度
	 * 8.36计算过程 身长6.79减去头顶到中部位置的长度2.6 再乘以2
	 *
	 * @return
	 */
	@Override
	public int getIntrinsicHeight() {
		return (int) (8.38f * HEAD_RADIUS);
	}

	@Override
	public int getIntrinsicWidth() {
		return (int) (8.38f * HEAD_RADIUS);
	}
}
