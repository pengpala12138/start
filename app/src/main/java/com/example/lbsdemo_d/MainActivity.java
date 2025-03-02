package com.example.lbsdemo_d;
import static com.baidu.mapapi.map.BaiduMap.MAP_TYPE_NORMAL;
import static com.baidu.mapapi.map.BaiduMap.MAP_TYPE_SATELLITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView locationInfo;//用于显示定位信息
    LocationClient mlocationClient;//声明一个LocationClient类型的变量，用于管理百度地图的定位功能
    MapView mMapView;//用于显示百度地图
    BaiduMap mBaiduMap = null;//管理百度地图的功能
    boolean isFirstLocate = true;//用于标记是否为第一次定位
    private PoiSearch mPoiSearch = null;// 声明一个私有变量 mPoiSearch，用于存储 PoiSearch 对象
    EditText mInputText;//搜索框
    BDLocation mCurlocation;//位置信息
    private PoiOverlay mPoiOverlay;//用于覆盖物
    private int mCurrentPage = 0; // 当前页码
    ListView listView;
    private List<PoiInfo> mAllPoiList = new ArrayList<>();//所有POI数据
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);



        SDKInitializer.setAgreePrivacy(getApplicationContext(), true); // 确保用户同意隐私政策
        SDKInitializer.initialize(getApplicationContext());//百度地图SDK初始化
        EdgeToEdge.enable(this);//启用全面屏显示
        setContentView(R.layout.activity_main);//设置布局文件
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        //将在前端的东西与后端建立联系（findViewById）
        mMapView = findViewById(R.id.bmapView);

        //获取地图实例化
        mBaiduMap = mMapView.getMap();
        //设置地图类型为普通视图
        mBaiduMap.setMapType(MAP_TYPE_NORMAL);
        //启用定位图层（显示蓝点）
        mBaiduMap.setMyLocationEnabled(true);
        initPoiOverlay();//Poi覆盖物初始化
        //设置地图监听器
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            //当地图被点击时调用
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();
            }//隐藏地图上的信息窗口
            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
            }
        });
        //  切换地图类型
        ImageButton matype = this.findViewById(R.id.mapTypeBtn);
        matype.setContentDescription(getString(R.string.map_type_icon_description));//无障碍阅读
        matype.setOnClickListener(new View.OnClickListener() {//设置地图类型切换按钮的点击事件
            @Override
            public void onClick(View v) {
                BaiduMap map = mMapView.getMap();//获取当前地图实例
                int type = map.getMapType();//获取当前地图类型
                switch (type) {
                    case MAP_TYPE_NORMAL:
                        map.setMapType(MAP_TYPE_SATELLITE);//从普通地图切换到卫星地图
                        break;
                    case MAP_TYPE_SATELLITE:
                        map.setMapType(MAP_TYPE_NORMAL);//从卫星地图切换到普通地图
                        break;
                }
            }
        });

        //定位客户端初始化
        //隐私合规
        LocationClient.setAgreePrivacy(true);
        //创建百度定位客户端
        try {
            mlocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //创建一个自定义的位置监听器实例
        MylocationListener mylocationListener = new MylocationListener();
        //自定义的位置监听器注册到百度地图定位客户端
        mlocationClient.registerLocationListener(mylocationListener);


        //定位按钮（回到当前位置）
        ImageButton locationBtn = this.findViewById(R.id.mylocation);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mylocationListener.setAutoLocation(true);//设置自动定位为真
                mlocationClient.start();//启动百度地图SDK中的定位客户端
            }
        });

        //poi检索实例
        mPoiSearch = PoiSearch.newInstance();
        //poi监听器
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        //获得检索输入框控制
        mInputText = findViewById(R.id.inputText);
        //设置mInputText的编辑器动作监听器
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {//当用户在编辑器中按下搜索按钮时触发
                boolean ret = false;
                //如果动作Id是搜索动作回车
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mCurrentPage = 0;
                    String city = mCurlocation.getCity();//获取当前城市
                    String KeyWord = v.getText().toString();//获取用户输入的关键词
                    ret = mPoiSearch.searchInCity(new PoiCitySearchOption()
                            .city(city)
                            .keyword(KeyWord)
                            .scope(2)//设置搜索范围
                            .pageCapacity(100)
                            .cityLimit(false)
                            .tag("美食")
                            .tag("旅游地点")
                            .tag("住宿")
                            .pageNum(mCurrentPage));

                    //搜索后隐藏键盘
                    InputMethodManager imum = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);//获取系统的输入法管理器
                    View view = getWindow().peekDecorView();//获取当前窗口的根视图
                    if (view != null) {
                        imum.hideSoftInputFromWindow(view.getWindowToken(), 0);//隐藏软键盘
                    }
                }
                return ret;
            }
        });

        //权限请求逻辑

        List<String> permissionList = new ArrayList<String>();//ArrayList是List接口的一个具体实现类，基于动态数组的实现，提供了对元素的快速随机访问
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE );
//        }
        if (!permissionList.isEmpty()) {//若permissionList里不为空，需要向用户申请权限
            String[] permissions = permissionList.toArray(new String[0]);//toArray-转换成数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();//请求位置信息
        }

        // 设置覆盖物点击监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 直接委托给PoiOverlay处理
                return mPoiOverlay.onMarkerClick(marker);
            }
        });

        // 初始化 ListView（搜索结果框）
        listView = findViewById(R.id.searchResult);
    }

    //创建poi检索监听器
    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            // 添加整体结果有效性检查
            if (poiResult == null || poiResult.getAllPoi() == null) {
                //Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (poiResult != null) {
                //检查是否为第一页的搜索结果
                if (poiResult.getCurrentPageNum() == 0) {
                    mPoiOverlay.removeFromMap();//清除覆盖物
                    mAllPoiList.clear();//清除搜索结果
                }
                //获取所有的POI信息
                List<PoiInfo> poiList = poiResult.getAllPoi();
                //处理分页数据前列表有效性
                if (poiList == null || poiList.isEmpty()) {
                    if (poiResult.getCurrentPageNum() == 0) {
                        Toast.makeText(MainActivity.this, "未找到相关结果", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "没有更多数据了", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                // 确保mAllPoiList已初始化
                if (mAllPoiList == null) {
                    mAllPoiList = new ArrayList<>();
                }
                // 添加新的POI信息
                mAllPoiList.addAll(poiList);

                // 添加新POI覆盖物
                mPoiOverlay.setData(mAllPoiList); // 自定义构造函数传递所有数据
                mPoiOverlay.addToMap();
                mPoiOverlay.zoomToSpan();
                // 更新适配器
                if (poiResult.getCurrentPageNum() == 0) {
                    // 第一页时创建新适配器
                    PoiAdapter adapter = new PoiAdapter(MainActivity.this, R.layout.poi_item, mAllPoiList);
                    listView.setAdapter(adapter);
                } else {
                    // 后续页通知数据更新
                    ((PoiAdapter) listView.getAdapter()).notifyDataSetChanged();
                    // 滚动到新数据位置
                    listView.smoothScrollToPosition(mAllPoiList.size() - poiList.size());
                }
                listView.setVisibility(View.VISIBLE);

                // 更新分页控制
                mCurrentPage = poiResult.getCurrentPageNum();

                //当滑动到底部时加载更多搜索结果
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        //当滚动停止时
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            //当滚动到底部时
                            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                                int curPage = poiResult.getCurrentPageNum();
                                int totalPage = poiResult.getTotalPageNum();
                                if (curPage < totalPage) {
                                    poiResult.setCurrentPageNum(curPage + 1);
                                    String city = mCurlocation.getCity();
                                    TextView textV = findViewById(R.id.inputText);
                                    String KeyWord = textV.getText().toString();
                                    //搜索下一页
                                    mPoiSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(KeyWord).pageNum(curPage + 1));
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "已加载全部数据", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }


                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });


            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

//处理权限请求结果，当定位权限被授予时开始定位，否则提示用户需要权限

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // 只检查定位权限是否被授予
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(this, "需要定位权限才能使用本程序", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

//权限请求成功后，启动定位
    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "定位权限未授予，无法进行定位", Toast.LENGTH_SHORT).show();
            return;
        }
        initLocation();
        mlocationClient.start();//启动百度地图SDK中的定位客户端

    }
//设置百度地图定位的各种参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();//配置百度地图定位的各种参数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式-高精度
        option.setCoorType("bd09ll");//设置百度经纬度坐标
        option.setScanSpan(1000);//定位时间间隔
        option.setOpenGps(true);//打开GPS

        option.setLocationNotify(true);//在定位状态改变时触发通知-由GPS-WIFI
        option.setIgnoreKillProcess(false);//应用进程被杀死时，客户端也会停止工作
        option.SetIgnoreCacheException(false);//不忽略缓存异常
        option.setWifiCacheTimeOut(5 * 60 * 1000);//wifi缓存信息五分钟过期一次，过期后重新获取定位信息
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);//是否需要地址信息，国家、省、市、区、街道等
        mlocationClient.setLocOption(option);//将配置好的选项应用到客户端

    }

    //监听定位结果
    private class MylocationListener extends BDAbstractLocationListener {

        boolean autoLocation=false;
        public void setAutoLocation(boolean b) {
            autoLocation=b;
        }
        //根据定位按钮修改
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null){
                return;
            }
            int type=bdLocation.getLocType();
            //构建MyLocaltionData对象，通过MyLocationData.Builder设置经纬度
            MyLocationData locData = new MyLocationData.Builder()
                    //设置定位精度
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            BaiduMap bmap=mMapView.getMap();//获取地图实例
            bmap.setMyLocationData(locData);//将定位数据设置到地图上
            /**
             *当首次定位或手动发起定位时，要放大地图，便于观察具体的位置
             * LatLng是缩放的中心点，这里注意一定要和上面设置给地图的经纬度一致；
             * MapStatus.Builder 地图状态构造器
             */
            if (isFirstLocate||autoLocation) {
                //将首次定位标志设置为false
                isFirstLocate = false;
                //将手动发起定位标志设置为false
                autoLocation=false;
                //创建Latlng对象，表示当前位置的经纬度
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                //创建地图状态构造器
                MapStatus.Builder builder = new MapStatus.Builder();
                //设置缩放中心点；缩放比例；
                builder.target(ll).zoom(18.0f);
                //给地图设置状态
                bmap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            mCurlocation = bdLocation;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);//禁用定位图层
        mlocationClient.stop();
    }

    @Override
    protected void onResume() {//暂停状态恢复到前台
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
        mMapView.onPause();
    }


    /**
     * 重写 dispatchTouchEvent 方法，用于处理触摸事件。
     * 当检测到触摸事件为 ACTION_DOWN（按下）时，判断触摸位置是否在搜索结果列表之外。
     * 如果在列表之外，则隐藏搜索结果列表。
     *
     * @param ev 触摸事件对象，包含触摸事件的详细信息，如触摸位置、动作类型等。
     * @return 返回调用父类的 dispatchTouchEvent 方法的结果，以确保触摸事件能正常分发。
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {//如果触摸事件为按下
            ListView listView = findViewById(R.id.searchResult);//获取搜索结果列表
            int[] location = new int[2];
            listView.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + listView.getWidth();
            int bottom = top + listView.getHeight();
            float x = ev.getRawX();//获取触摸事件的原始x坐标
            float y = ev.getRawY();//获取触摸事件的原始y坐标
            if (x < left || x > right || y < top || y > bottom) {
                // 点击搜索结果列表之外区域，隐藏搜索结果列表
                listView.setVisibility(View.GONE);
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    //初始化覆盖物
    private void initPoiOverlay() {
        // 初始化 PoiOverlay
        mPoiOverlay = new PoiOverlay(mBaiduMap,this,mAllPoiList) {
            @Override
            // 覆写此方法以改变默认点击行为
            public boolean onPoiClick(int index) {
                // 处理点击事件
                if (index < mAllPoiList.size()) {
                    // 获取点击的 PoiInfo
                    PoiInfo poiInfo = mAllPoiList.get(index);
                    // 显示 PoiInfo 的信息窗口
                    showPoiInfoWindow(poiInfo);
                    return true;
                }
                return false;
            }
        };

        //为百度地图设置标记点击监听器
        mBaiduMap.setOnMarkerClickListener(marker -> {
            if (mPoiOverlay.getOverlayOptions().contains(marker)) {//如果mPoiOverlay的覆盖物列表中包含该marker
                return mPoiOverlay.onMarkerClick(marker);//调用mPoiOverlay的onMarkerClick方法
            }
            return false;
        });
    }



    // 显示 PoiInfo 的信息窗口
    private void showPoiInfoWindow(PoiInfo poi) {
        //隐藏当前显示的信息窗口
        mBaiduMap.hideInfoWindow();
        // 加载自定义布局
        View infoWindow = LayoutInflater.from(this).inflate(R.layout.poi_info_window, null);
        //设置信息窗口的内边距
        infoWindow.setPadding(20, 20, 20, 20);
        //获取布局中的TextView和Button
        TextView title = infoWindow.findViewById(R.id.tv_title);
        TextView address = infoWindow.findViewById(R.id.tv_address);
        Button navigateBtn = infoWindow.findViewById(R.id.btn_navigate);
        //设置TextView的文本为兴趣点名称和地址
        title.setText(poi.getName());
        address.setText(poi.getAddress());
        //计算信息窗口的偏移量
        Point screenPosition = mBaiduMap.getProjection().toScreenLocation(poi.getLocation());
        int yOffset = (screenPosition.y > mMapView.getHeight()/2) ? -200 : 200;
        // 创建信息窗口
        InfoWindow mInfoWindow = new InfoWindow(infoWindow, poi.getLocation(), -150);

        // 显示信息窗口
        mBaiduMap.showInfoWindow(mInfoWindow);

        // 处理导航按钮点击
        navigateBtn.setOnClickListener(v -> {
            if (poi.getLocation() != null) {
                PoiAdapter.startNavigation(
                        MainActivity.this,
                        poi.getLocation().latitude,
                        poi.getLocation().longitude,
                        poi.getName()
                );
                mBaiduMap.hideInfoWindow(); // 隐藏信息窗口
            }
        });
    }

}


