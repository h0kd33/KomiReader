package moe.komi.reader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import com.fmsirvent.ParallaxEverywhere.PEWImageView;

/**
 * Created by peter on 3/2/15.
 */
public class PostViewFirstImageView extends PEWImageView {
    public static final String TAG = "PostFirstImage";

    public PostViewFirstImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostViewFirstImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // http://stackoverflow.com/questions/7719617/imageview-adjustviewbounds-not-working
        if (Build.VERSION.SDK_INT < 18) {
            Drawable mDrawable = getDrawable();
            if (mDrawable != null) {
                int mDrawableWidth = mDrawable.getIntrinsicWidth();
                int mDrawableHeight = mDrawable.getIntrinsicHeight();
                float actualAspect = (float) mDrawableWidth / (float) mDrawableHeight;

                int minHeight = getMyMinHeight();
                int maxHeight = getMyMaxHeight();

                // Assuming the width is ok, so we calculate the height.
                final int actualWidth = MeasureSpec.getSize(widthMeasureSpec);
                final int height = Math.max(minHeight, Math.min(maxHeight, (int) (actualWidth / actualAspect)));
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getMyMinHeight() {
        return (int) Math.round(getResources().getDimension(R.dimen.post_header_height));
    }

    private int getMyMaxHeight() {
        return (int) Math.round(getResources().getDimension(R.dimen.post_header_maxheight));
    }

    //todo 16:9
    /*
    @Override
    protected void onAttachedToWindow() {
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float w = (float) getWidth();
                int h = (int)Math.round(w * 0.5625);
                setMinimumHeight(h);
                setMaxHeight(h);
            }
        };

        getViewTreeObserver().addOnGlobalLayoutListener(listener);

       super.onAttachedToWindow();
    }

    */
}
