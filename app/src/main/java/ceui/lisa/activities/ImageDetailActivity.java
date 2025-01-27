package ceui.lisa.activities;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ceui.lisa.R;
import ceui.lisa.databinding.ActivityImageDetailBinding;
import ceui.lisa.download.IllustDownload;
import ceui.lisa.fragments.FragmentImageDetail;
import ceui.lisa.fragments.FragmentLocalImageDetail;
import ceui.lisa.helper.PageTransformerHelper;
import ceui.lisa.models.IllustsBean;
import ceui.lisa.utils.Common;

/**
 * 图片二级详情
 */
public class ImageDetailActivity extends BaseActivity<ActivityImageDetailBinding> {

    private IllustsBean mIllustsBean;
    private List<String> localIllust = new ArrayList<>();
    private TextView currentPage, downloadSingle, currentSize;
    private int index;

    @Override
    protected int initLayout() {
        refreshSystemUiVisibility();
        return R.layout.activity_image_detail;
    }

    @Override
    protected void initView() {
        String dataType = getIntent().getStringExtra("dataType");
        baseBind.viewPager.setPageTransformer(true, PageTransformerHelper.getCurrentTransformer());
        if ("二级详情".equals(dataType)) {
            currentSize = findViewById(R.id.current_size);
            currentPage = findViewById(R.id.current_page);
            downloadSingle = findViewById(R.id.download_this_one);
            mIllustsBean = (IllustsBean) getIntent().getSerializableExtra("illust");
            index = getIntent().getIntExtra("index", 0);
            if (mIllustsBean == null) {
                return;
            }
            baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int i) {
                    return FragmentImageDetail.newInstance(mIllustsBean, i);
                }

                @Override
                public int getCount() {
                    return mIllustsBean.getPage_count();
                }
            });
            baseBind.viewPager.setCurrentItem(index);
            checkDownload(index);
            downloadSingle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IllustDownload.downloadIllustCertainPage(mIllustsBean, baseBind.viewPager.getCurrentItem(), (BaseActivity<?>) mContext);
                }
            });
            baseBind.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    checkDownload(i);
                    currentPage.setText(String.format(Locale.getDefault(), "第 %d/%d P", i + 1, mIllustsBean.getPage_count()));
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
            if(mIllustsBean.getPage_count() == 1){
                currentPage.setVisibility(View.INVISIBLE);
            }else{
                currentPage.setText(String.format(Locale.getDefault(), "第 %d/%d P", index + 1, mIllustsBean.getPage_count()));
            }

        } else if ("下载详情".equals(dataType)) {
            currentPage = findViewById(R.id.current_page);
            downloadSingle = findViewById(R.id.download_this_one);
            localIllust = (List<String>) getIntent().getSerializableExtra("illust");
            index = getIntent().getIntExtra("index", 0);

            baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int i) {
                    return FragmentLocalImageDetail.newInstance(localIllust.get(i));
                }

                @Override
                public int getCount() {
                    return localIllust.size();
                }
            });
            currentPage.setVisibility(View.INVISIBLE);
            baseBind.viewPager.setCurrentItem(index);
            baseBind.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    try {
                        downloadSingle.setText(String.format("%s%s", getString(R.string.file_path),
                                URLDecoder.decode(localIllust.get(i), "utf-8")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
            try {
                downloadSingle.setText(String.format("%s%s", getString(R.string.file_path),
                        URLDecoder.decode(localIllust.get(index), "utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkDownload(int i) {
        downloadSingle.setVisibility(Common.isIllustDownloaded(mIllustsBean, i) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void initData() {
        postponeEnterTransition();
    }

    @Override
    public void onBackPressed() {
        if (index == baseBind.viewPager.getCurrentItem()) {
            super.onBackPressed();
        } else {
            mActivity.finish();
        }
    }

    @Override
    public boolean hideStatusBar() {
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            refreshSystemUiVisibility();
        }
    }

    private void refreshSystemUiVisibility() {
        final int includeFlag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        int excludeFlag = 0;
        if (Shaft.sSettings.isIllustDetailShowNavbar()) {
            excludeFlag = excludeFlag
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        final int flag = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(flag | includeFlag & ~excludeFlag);
    }
}
