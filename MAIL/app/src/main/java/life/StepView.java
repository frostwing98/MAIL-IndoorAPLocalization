package life;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.wifi.ScanResult;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import life.Jama.Polyfit;
import life.Jama.Polyval;
import life.util.Circle;

public class StepView extends View {
    private Paint mPaint;
    private Paint mStrokePaint;
    private Path mArrowPath; // 箭头路径
    private boolean draw=false;
    private int cR = 10; // 圆点半径
    private int arrowR = 20; // 箭头半径

    private float mCurX = 200;
    private float mCurY = 200;
    private int mOrient;
    private Bitmap mBitmap;
    private ArrayList<Circle> circlelist=new ArrayList<>();
//    private double wifisiglevel=0.0;
    private ArrayList<Double> siglevellist=new ArrayList<>();
    private List<PointF> mPointList = new ArrayList<>();
    private ArrayList<Map<String,Double>> resultmaplist=new ArrayList<>();
    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mStrokePaint = new Paint(mPaint);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(5);

        // 初始化箭头路径
        mArrowPath = new Path();
        mArrowPath.arcTo(new RectF(-arrowR, -arrowR, arrowR, arrowR), 0, -180);
        mArrowPath.lineTo(0, -3 * arrowR);
        mArrowPath.close();

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), new Rect(0, 0, getWidth(), getHeight()), null); // 将mBitmap绘到canLock

        for (int i=0;i<mPointList.size();i++) {
            PointF p=mPointList.get(i);
//            Double d=siglevellist.get(i);
//            if(d<-80.0){
//                mPaint.setColor(Color.rgb(0,255,255	));//cyan, (0,20)
//            }else if(d<-60.0){
//                mPaint.setColor(Color.rgb(0,255,127		));//green, (20, 40)
//            }else if(d<-40.0){
//                mPaint.setColor(Color.rgb(255 ,255,0));//yellow, (40,60)
//            }else if(d<-20.0){
//                mPaint.setColor(Color.rgb(255,0,0));//red, (60,80)
//            }else if(d<-0.0){
//                mPaint.setColor(Color.rgb(139,26,26));//brickred, (80,100)
//            }

            canvas.drawCircle(p.x, p.y, cR, mPaint);
//            canvas.drawText(""+siglevellist.get(i),p.x+10,p.y,mPaint);
//            i++;
        }
            for(Circle c:circlelist){
                Paint cPaint=new Paint();
                cPaint.setColor(Color.RED);
                cPaint.setAlpha(128);
                cPaint.setAntiAlias(true);
                cPaint.setStyle(Paint.Style.FILL);
                Paint txtPaint=new Paint();
                txtPaint.setColor(Color.GREEN);
                txtPaint.setAntiAlias(true);
                txtPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(c.x,c.y,c.r,cPaint);
                canvas.drawText(c.name,c.x,c.y,txtPaint);
                invalidate();
            }


        canvas.save(); // 保存画布
        invalidate();
        canvas.translate(mCurX, mCurY); // 平移画布
        canvas.rotate(mOrient); // 转动画布
        canvas.drawPath(mArrowPath, mPaint);
        canvas.drawArc(new RectF(-arrowR * 0.8f, -arrowR * 0.8f, arrowR * 0.8f, arrowR * 0.8f),
                0, 360, false, mStrokePaint);

        canvas.restore(); // 恢复画布
    }

    /**
     * 当屏幕被触摸时调用
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurX = event.getX();
        mCurY = event.getY();
        invalidate();
        return true;
    }

    /**
     * 自动增加点
     */
    public void autoAddPoint(float stepLen, Map<String,Double> resultmap) {
        mCurX += (float) (stepLen * Math.sin(Math.toRadians(mOrient)));
        mCurY += -(float) (stepLen * Math.cos(Math.toRadians(mOrient)));
        mPointList.add(new PointF(mCurX, mCurY));
        ScanResult sc=null;
        resultmaplist.add(resultmap);

        invalidate();
    }

    public void autoDrawArrow(int orient) {
        mOrient = orient;
        invalidate();
    }
    public void drawcircle(){
        draw=true;
        if(resultmaplist.size()==0||resultmaplist==null){
            return;
        }
        HashMap<String,ArrayList<Double>> tempmap=new HashMap<>();
        for(String s:resultmaplist.get(0).keySet()){
            tempmap.put(s,new ArrayList<Double>());
            tempmap.get(s).add(resultmaplist.get(0).get(s));
        }
        if(resultmaplist.size()>=2){
            for(int i=1;i<resultmaplist.size();i++){
                for(String wifiname:resultmaplist.get(0).keySet()){
                    if(resultmaplist.get(i).get(wifiname)==null)
                        tempmap.get(wifiname).add(0.0);
                    else
                        tempmap.get(wifiname).add(resultmaplist.get(i).get(wifiname));
                }
            }
        }
        System.out.println("WIFI COUNT:"+tempmap.size());
        circlelist=new ArrayList<>();

        for(String wifiname:tempmap.keySet()){
            Circle c=calculatePosition(tempmap.get(wifiname));
            c.name=wifiname;
            circlelist.add(c);
            System.out.println(c.x+" "+c.y+" "+c.r);

        }

    }
    public Circle calculatePosition(ArrayList<Double> list){
        System.out.println("INTERESTING: ");
        for(double d:list){
            System.out.print(" "+d);
        }
        double [] x = new double[list.size()];
        double [] y = new double[list.size()];
        for(int i=0;i<list.size();i++){
            x[i]=i+1;
            y[i]=list.get(i);
        }
        Polyfit polyfit = null;
        Polyval polyval;
        double[] fitted=new double[list.size()];
        try {
            polyfit = new Polyfit(x, y, 3);
            polyval = new Polyval(x, polyfit);
            for (int i = 0; i <= polyval.getYout().length - 1;i++){
                BigDecimal bd = new BigDecimal(polyval.getYout()[i]);
//                setScale(2, BigDecimal.ROUND_HALF_UP);
//                System.out.println("HELLO "+(i + 1) + "\t" + bd.toString());
                fitted[i]=Double.parseDouble(bd.toString());
            }
        }catch (Exception e) {
            System.out.println ("Error:" + e.getMessage () + "\n");
            e.printStackTrace ();
        }

        double maximal=0.0;
        int maxindex=0;
        for(int i=0;i<fitted.length;i++){
            if(maximal<=fitted[i]){
                maximal=fitted[i];
                maxindex=i;
            }
        }
        int leftbound=maxindex-1;
        int rightbound=maxindex+1;
        int i=0;
        while(leftbound>=0){
            if(Math.abs(x[leftbound]-x[maxindex])<=2.5){
                leftbound--;
            }else{
                break;
            }
        }
        leftbound++;
        while(rightbound<list.size()){
            if(Math.abs(x[rightbound]-x[maxindex])<=2.5){
                rightbound++;
            }else{
                break;
            }
        }
        rightbound--;
        System.out.println(maxindex+" "+leftbound+" "+rightbound);
        PointF leftpoint=mPointList.get(leftbound);
        PointF midpoint=mPointList.get(maxindex);
        PointF rightpoint=mPointList.get(rightbound);
        double px=(leftpoint.x+rightpoint.x)/2.0;
        double py=(leftpoint.y+rightpoint.y)/2.0;
        double pr=Math.sqrt(Math.pow(leftpoint.x-rightpoint.x,2)+Math.pow(leftpoint.y-rightpoint.y,2));
        return new Circle(null,(float)px,(float)py,(float)pr);

    }
}