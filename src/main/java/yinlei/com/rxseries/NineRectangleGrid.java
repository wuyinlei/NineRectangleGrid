package yinlei.com.rxseries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义九宫格
 *
 * @version V1.0 <描述当前版本功能>
 * @FileName: LockPatternView.java
 * @author: 若兰明月
 * @date: 2016-06-27 17:50
 */

public class NineRectangleGrid extends View {

    //NineRectangleGrid

    //选中点的数量
    private static final int POINT_SIZE = 5;
    //矩阵
    private Matrix matrix = new Matrix();

    //画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Point[][] mPoint = new Point[3][3];
    private boolean isInit;

    //监听器
    private OnPatterChangeListener mOnPatterChangeListener;

    //按下的点的集合
    private List<Point> mPointList = new ArrayList<>();

    private float width, height, offsetsX, offsetsY, bitmapR, movingX, movingY;
    private Bitmap pointNormal, pointPress, pointError, linePress, lineError;
    private boolean isSelect = false;
    private boolean isFinish, movingNoPoint = false;

    public NineRectangleGrid(Context context) {
        super(context);
    }

    public NineRectangleGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NineRectangleGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInit) {
            initPoints();
        }
        //画点
        points2Canvas(canvas);

        //画线
        if (mPointList.size() > 0) {
            Point a = mPointList.get(0);
            for (int i = 0; i < mPointList.size(); i++) {
                Point b = mPointList.get(i);
                line2Canvas(canvas, a, b);
                a = b;
            }
            if (movingNoPoint) {
                //绘制鼠标
                line2Canvas(canvas, a, new Point(movingX, movingY));
            }
        }
    }

    /**
     * 绘制
     *
     * @param canvas 画布
     */
    private void points2Canvas(Canvas canvas) {
        for (int i = 0; i < mPoint.length; i++) {
            for (int j = 0; j < mPoint[i].length; j++) {
                Point point = mPoint[i][j];
                if (point.state == Point.STATE_PRESSED) {
                    canvas.drawBitmap(pointPress, point.x - bitmapR, point.y - bitmapR, paint);
                } else if (point.state == Point.STATE_ERROR) {
                    canvas.drawBitmap(pointError, point.x - bitmapR, point.y - bitmapR, paint);
                } else if (point.state == Point.STATE_NORMAL) {
                    canvas.drawBitmap(pointNormal, point.x - bitmapR, point.y - bitmapR, paint);
                }
            }
        }
    }

    /**
     * @param canvas 画布
     * @param a      第一个点
     * @param b      第二个点
     */
    private void line2Canvas(Canvas canvas, Point a, Point b) {

        double lineLength = Point.distance(a, b);

        float degress = getDegrees(a, b);
        canvas.rotate(degress, a.x, a.y);
        if (a.state == Point.STATE_PRESSED) {
            getDegrees(a, b);
            matrix.setScale((float) (lineLength / linePress.getWidth()), 1);
            matrix.postTranslate(a.x - linePress.getWidth() / 2, a.y - linePress.getHeight() / 2);
            canvas.drawBitmap(linePress, matrix, paint);
        } else {
            matrix.setScale((float) (lineLength / lineError.getWidth()), 1);
            matrix.postTranslate(a.x - lineError.getWidth() / 2, a.y - lineError.getHeight() / 2);
            canvas.drawBitmap(lineError, matrix, paint);
        }
        canvas.rotate(-degress, a.x, a.y);
    }

    private float getDegrees(Point a, Point b) {
        float ax = a.x;// a.index % 3;
        float ay = a.y;// a.index / 3;
        float bx = b.x;// b.index % 3;
        float by = b.y;// b.index / 3;
        float degrees = 0;
        if (bx == ax) // y轴相等 90度或270
        {
            if (by > ay) // 在y轴的下边 90
            {
                degrees = 90;
            } else if (by < ay) // 在y轴的上边 270
            {
                degrees = 270;
            }
        } else if (by == ay) // y轴相等 0度或180
        {
            if (bx > ax) // 在y轴的下边 90
            {
                degrees = 0;
            } else if (bx < ax) // 在y轴的上边 270
            {
                degrees = 180;
            }
        } else {
            if (bx > ax) // 在y轴的右边 270~90
            {
                if (by > ay) // 在y轴的下边 0 - 90
                {
                    degrees = 0;
                    degrees = degrees
                            + switchDegrees(Math.abs(by - ay),
                            Math.abs(bx - ax));
                } else if (by < ay) // 在y轴的上边 270~0
                {
                    degrees = 360;
                    degrees = degrees
                            - switchDegrees(Math.abs(by - ay),
                            Math.abs(bx - ax));
                }

            } else if (bx < ax) // 在y轴的左边 90~270
            {
                if (by > ay) // 在y轴的下边 180 ~ 270
                {
                    degrees = 90;
                    degrees = degrees
                            + switchDegrees(Math.abs(bx - ax),
                            Math.abs(by - ay));
                } else if (by < ay) // 在y轴的上边 90 ~ 180
                {
                    degrees = 270;
                    degrees = degrees
                            - switchDegrees(Math.abs(bx - ax),
                            Math.abs(by - ay));
                }

            }
        }
        return degrees;
    }

    /**
     * 1=30度 2=45度 4=60度
     *
     * @param
     * @return
     */
    private float switchDegrees(float x, float y) {
        return (float) Point.pointTotoDegrees(x, y);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        movingX = event.getX();
        movingY = event.getY();
        movingNoPoint = false;
        isFinish = false;

        Point point = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mOnPatterChangeListener != null) {
                    mOnPatterChangeListener.onPatterStart(true);
                }
                resets();
                point = checkSelectPoint();
                if (point != null) {
                    isSelect = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelect) {
                    point = checkSelectPoint();
                    if (point == null) {
                        movingNoPoint = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isFinish = true;
                isSelect = false;
                break;

        }
        if (!isFinish && isSelect && point != null) {
            if (crossPoint(point)) {
                movingNoPoint = true;
            } else {
                point.state = Point.STATE_PRESSED;
                mPointList.add(point);

            }
        }
        //完成时候绘制
        if (isFinish) {
            if (mPointList.size() == 1) {
                resets();

                //绘制失败
            } else if (mPointList.size() < POINT_SIZE && mPointList.size() >= 2) {
                errorPoint();
                if (mOnPatterChangeListener != null) {
                    mOnPatterChangeListener.onPatterChange(null);
                }
                //绘制成功
            } else {
                if (mOnPatterChangeListener != null) {
                    String passwordStr = "";
                    for (int i = 0; i < mPointList.size(); i++) {
                        passwordStr += mPointList.get(i).index;
                    }
                    mOnPatterChangeListener.onPatterChange(passwordStr);
                }
            }
        }
        //刷新View
        postInvalidate();
        return true;
    }

    //重置
    public void resets() {
        for (int i = 0; i < mPointList.size(); i++) {
            Point point = mPointList.get(i);
            point.state = Point.STATE_NORMAL;
        }
        mPointList.clear();
    }

    //错误的点
    public void errorPoint() {
        for (Point point : mPointList
                ) {
            point.state = Point.STATE_ERROR;

        }
    }

    /**
     * @param point
     * @return 是否已经选择过
     */
    private boolean crossPoint(Point point) {
        if (mPointList.contains(point)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查是否选中
     */
    private Point checkSelectPoint() {

        for (int i = 0; i < mPoint.length; i++) {
            for (int j = 0; j < mPoint[i].length; j++) {
                Point point = mPoint[i][j];
                if (Point.with(point.x, point.y, bitmapR, movingX, movingY)) {
                    return point;
                }
            }
        }
        return null;
    }

    //初始化点
    private void initPoints() {
        /**获取宽高*/
        width = getWidth();
        height = getHeight();

        //确定是横屏还是竖屏
        if (width > height) {
            //横屏
            offsetsX = (width - height) / 2;
            width = height;
        } else {
            //竖屏
            offsetsY = (height - width) / 2;
            height = width;
        }

        //图片资源
        pointNormal = BitmapFactory.decodeResource(getResources(), R.mipmap.locus_round_normal);
        pointPress = BitmapFactory.decodeResource(getResources(), R.mipmap.locus_round_press);
        pointError = BitmapFactory.decodeResource(getResources(), R.mipmap.locus_round_click_error);
        lineError = BitmapFactory.decodeResource(getResources(), R.mipmap.locus_line);
        linePress = BitmapFactory.decodeResource(getResources(), R.mipmap.locus_line_error);


        //绘制九个点
        mPoint[0][0] = new Point(offsetsX + width / 4, offsetsY + width / 4);
        mPoint[0][1] = new Point(offsetsX + width / 2, offsetsY + width / 4);
        mPoint[0][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 4);

        mPoint[1][0] = new Point(offsetsX + width / 4, offsetsY + width / 2);
        mPoint[1][1] = new Point(offsetsX + width / 2, offsetsY + width / 2);
        mPoint[1][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 2);

        mPoint[2][0] = new Point(offsetsX + width / 4, offsetsY + width - width / 4);
        mPoint[2][1] = new Point(offsetsX + width / 2, offsetsY + width - width / 4);
        mPoint[2][2] = new Point(offsetsX + width - width / 4, offsetsY + width - width / 4);

        //5 图片资源的半径
        bitmapR = pointNormal.getHeight() / 2;

        //6 设置密码
        int index = 1;
        for (Point[] points : this.mPoint) {
            for (Point point : points) {
                point.index = index;
                index++;
            }
        }

        //7 重新初始化
        isInit = true;

    }

    /**
     * 自定义的点
     */
    public static class Point {
        //正常
        public static int STATE_NORMAL = 0;
        //按下
        public static int STATE_PRESSED = 1;
        //错误
        public static int STATE_ERROR = 2;

        public float x, y;
        public int index = 0, state = 0;

        public Point() {

        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }


        /**
         * @param a 点a
         * @param b 点b
         * @return 距离
         */
        public static double distance(Point a, Point b) {
            return Math.sqrt(Math.abs(a.x - b.x) * Math.abs(a.x - b.x) + Math.abs(a.y - b.y) * Math.abs(a.y - b.y));
        }

        /**
         * @param pointX  参考点的x
         * @param pointY  参考点的y
         * @param r       圆的半径
         * @param movingX 移动点的x
         * @param movingY 移动点的y
         * @return 是否重合
         */
        public static boolean with(float pointX, float pointY, float r, float movingX, float movingY) {
            return Math.sqrt((pointX - movingX) * (pointX - movingX) + (pointY - movingY) * (pointY - movingY)) < r;
        }

        /**
         * 计算点a(x,y)的角度
         *
         * @param x
         * @param y
         * @return
         */
        public static double pointTotoDegrees(double x, double y) {
            return Math.toDegrees(Math.atan2(x, y));
        }
    }

    /**
     * 图案监听器
     */
    public interface OnPatterChangeListener {
        /**
         * 图案改变
         *
         * @param passwordStr
         */
        void onPatterChange(String passwordStr);

        /**
         * 重新开始绘制
         *
         * @param isStart
         */
        void onPatterStart(boolean isStart);
    }

    /**
     * 设置图案监听器
     *
     * @param onPatterChangeListener
     */
    public void setOnPatterChangeListener(OnPatterChangeListener onPatterChangeListener) {
        if (onPatterChangeListener != null) {
            this.mOnPatterChangeListener = onPatterChangeListener;
        }
    }

}
