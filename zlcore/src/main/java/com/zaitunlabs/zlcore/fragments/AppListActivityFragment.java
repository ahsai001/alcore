package com.zaitunlabs.zlcore.fragments;

import android.content.Context;
import androidx.annotation.Nullable;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.GsonBuilder;
import com.zaitunlabs.zlcore.R;
import com.zaitunlabs.zlcore.adapters.AppListAdapter;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.tables.AppListDataModel;
import com.zaitunlabs.zlcore.tables.AppListModel;
import com.zaitunlabs.zlcore.tables.AppListPagingModel;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.listeners.RecyclerViewLoadMoreListener;
import com.zaitunlabs.zlcore.listeners.RecyclerViewTouchListener;
import com.zaitunlabs.zlcore.utils.CommonUtil;
import com.zaitunlabs.zlcore.utils.HttpClientUtil;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppListActivityFragment extends BaseFragment {
    public static final String PARAM_IS_MEID = InfoFragment.PARAM_IS_MEID;
    private AppListAdapter adapter;
    private List<AppListDataModel> appListDataModels = new ArrayList<>();
    private CustomRecylerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    private int countPerPage = 10;
    private int nextPage = 1;
    private AppListModel appListModel;

    public AppListActivityFragment() {
    }

    public void setArg(boolean isMeid){
        Bundle b = new Bundle();
        b.putBoolean(PARAM_IS_MEID, isMeid);
        setArguments(b);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AppListAdapter(appListDataModels);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (CustomRecylerView) view.findViewById(R.id.applist_recylerView);
        emptyView = view.findViewById(R.id.applist_empty_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.applist_refreshLayout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setEmptyView(emptyView);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLoadMoreListener(new RecyclerViewLoadMoreListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore(getContext(),nextPage,appListModel);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getActivity(), recyclerView, new RecyclerViewTouchListener.RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                String unique = appListDataModels.get(position).getUnique();
                String url = appListDataModels.get(position).getUrl();

                if (!TextUtils.isEmpty(unique)){
                    CommonUtil.openPlayStore(view.getContext(),unique);
                } else if (!TextUtils.isEmpty(url) && URLUtil.isValidUrl(url)){
                    CommonUtil.openBrowser(view.getContext(),url);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initAppListData(getActivity(), true);
            }
        });

        swipeRefreshLayout.setEnabled(true);

        initAppListData(getActivity(), false);
    }




    private void initAppListData(Context context, boolean forceRefresh) {
        AppListModel appListModel = AppListModel.getLastCache();
        if(appListModel == null || forceRefresh) {
            fetchAppListData(context,1,appListModel);
        } else {
            loadInitList(appListModel.getData());
            nextPage = appListModel.getPaging().getNext();
            AppListActivityFragment.this.appListModel = appListModel;
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadInitList(List<AppListDataModel> appListDataModelList){
        recyclerView.initLoadMore();
        appListDataModels.clear();
        if(appListDataModelList != null) {
            appListDataModels.addAll(appListDataModelList);
        }
        adapter.notifyDataSetChanged();
    }

    private void addList(List<AppListDataModel> appListDataModelList){
        if(appListDataModelList != null) {
            appListDataModels.addAll(appListDataModelList);
        }
        adapter.notifyDataSetChanged();
    }


    private void loadMore(Context context,int loadingPage, AppListModel appListModel){
        if(loadingPage > -1) {
            adapter.showProgress();
            recyclerView.smoothScrollToPosition(adapter.getItemCount());
            fetchAppListData(context, loadingPage, appListModel);
        }
    }


    private void fetchAppListData(final Context context, final int loadingPage, final AppListModel appListModel){
        final boolean isMeid = CommonUtil.getBooleanFragmentArgument(getArguments(), PARAM_IS_MEID, false);

        AndroidNetworking.get(APIConstant.API_OTHER_APPS +"/"+countPerPage+"/"+loadingPage)
                .setOkHttpClient(HttpClientUtil.getHTTPClient(context, APIConstant.API_VERSION, isMeid))
                .setPriority(Priority.HIGH)
                .setTag("othersapp")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int status = response.optInt("status");
                        if(status <= 0){
                            swipeRefreshLayout.setRefreshing(false);
                            adapter.hideProgress();
                            String message = response.optString("message");
                            CommonUtil.showSnackBar(context,message);
                            return;
                        }
                        AppListModel responseListModel = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                                .create()
                                .fromJson(response.toString(), AppListModel.class);
                        if(loadingPage == 1) {
                            //delete and save new applistmodel
                            responseListModel.cache(true);
                            loadInitList(responseListModel.getData());
                            AppListActivityFragment.this.appListModel = responseListModel;

                        } else {
                            AppListPagingModel newAppListPagingModel = responseListModel.getPaging();

                            AppListPagingModel oldAppListPagingModel = AppListActivityFragment.this.appListModel.getPaging();
                            AppListActivityFragment.this.appListModel.setPaging(newAppListPagingModel);
                            AppListActivityFragment.this.appListModel.addNewDataListToCache(responseListModel.getData());
                            //AppListActivityFragment.this.appListModel.update();


                            //save new paging
                            newAppListPagingModel.appListModel = AppListActivityFragment.this.appListModel;
                            newAppListPagingModel.save();

                            //delete old paging
                            oldAppListPagingModel.delete();

                            adapter.hideProgress();

                            addList(responseListModel.getData());

                        }
                        nextPage = responseListModel.getPaging().getNext();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(ANError anError) {
                        if(loadingPage == 1) {
                            List<AppListDataModel> appListDataModels = null;
                            if(appListModel != null){
                                appListDataModels = appListModel.getData();
                            }
                            loadInitList(appListDataModels);
                        } else {
                            adapter.hideProgress();
                        }
                        CommonUtil.showSnackBar(context,anError.getErrorDetail());
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }


    @Override
    public void onDestroyView() {
        AndroidNetworking.cancel("othersapp");
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_store,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh){
            swipeRefreshLayout.setRefreshing(true);
            initAppListData(getActivity(), true);
        }
        return super.onOptionsItemSelected(item);
    }
}
