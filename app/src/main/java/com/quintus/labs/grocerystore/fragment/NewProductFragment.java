package com.quintus.labs.grocerystore.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.quintus.labs.grocerystore.R;
import com.quintus.labs.grocerystore.adapter.NewProductAdapter;
import com.quintus.labs.grocerystore.api.clients.RestClient;
import com.quintus.labs.grocerystore.model.PopularProducts;
import com.quintus.labs.grocerystore.model.PopularProductsResult;
import com.quintus.labs.grocerystore.model.Product;
import com.quintus.labs.grocerystore.model.ProductResult;
import com.quintus.labs.grocerystore.model.Token;
import com.quintus.labs.grocerystore.model.User;
import com.quintus.labs.grocerystore.util.localstorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * Grocery App
 * https://github.com/quintuslabs/GroceryStore
 * Created on 18-Feb-2019.
 * Created by : Santosh Kumar Dash:- http://santoshdash.epizy.com
 */

/**
 * A simple {@link Fragment} subclass.
 */
public class NewProductFragment extends Fragment {
    RecyclerView nRecyclerView;


    View progress;
    LocalStorage localStorage;
    Gson gson = new Gson();
    User user;
    String token;
    List<PopularProductsResult> productList = new ArrayList<>();
    private NewProductAdapter pAdapter;
    int page = 1;
    int page_size = 10;
    boolean isLoading = true;

    public NewProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new, container, false);
        nRecyclerView = view.findViewById(R.id.new_product_rv);

        progress = view.findViewById(R.id.progress_bar);

        localStorage = new LocalStorage(getContext());
        user = gson.fromJson(localStorage.getUserLogin(), User.class);
        token = localStorage.getApiKey();

        getNewProduct();

        return view;
    }


    private void getNewProduct() {
        showProgressDialog();
        Call<PopularProducts> call = RestClient.getRestService(getContext()).newProducts(token, page, page_size);
        call.enqueue(new Callback<PopularProducts>() {
            @Override
            public void onResponse(Call<PopularProducts> call, Response<PopularProducts> response) {
                Log.d("Response :=>", response.body() + "");
                if (response != null) {

                    PopularProducts productResult = response.body();
                    if (response.code() == 200) {

                        productList = productResult.getResults();
                        setupProductRecycleView();

                        if (page < productResult.getTotalPages()) {
                            isLoading = true;
                        } else {
                            isLoading = false;
                        }

                        initScrollListener();

                    }

                }

                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<PopularProducts> call, Throwable t) {
                Log.d("Error", t.getMessage());
                hideProgressDialog();

            }
        });
    }

    private void setupProductRecycleView() {
        pAdapter = new NewProductAdapter(productList, getContext(), "new");
        RecyclerView.LayoutManager pLayoutManager = new LinearLayoutManager(getContext());
        nRecyclerView.setLayoutManager(pLayoutManager);
        nRecyclerView.setItemAnimator(new DefaultItemAnimator());
        nRecyclerView.setAdapter(pAdapter);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("New");
    }

    private void hideProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    private void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }


    private void initScrollListener() {
        nRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productList.size() - 1) {
                        //bottom of list!
                        loadMore();

                    }
                }
            }
        });

    }

    private void loadMore() {
        page = page + 1;

        showProgressDialog();
        Call<PopularProducts> call = RestClient.getRestService(getContext()).newProducts(token, page, page_size);
        call.enqueue(new Callback<PopularProducts>() {
            @Override
            public void onResponse(Call<PopularProducts> call, Response<PopularProducts> response) {
                Log.d("Response :=>", response.body() + "");
                if (response != null) {

                    PopularProducts productResult = response.body();
                    if (response.code() == 200) {

                        productList.addAll( productResult.getResults());
                        pAdapter.notifyDataSetChanged();

                        if (page < productResult.getTotalPages()) {
                            isLoading = true;
                        } else {
                            isLoading = false;
                        }

                        initScrollListener();

                    }

                }

                hideProgressDialog();
            }

            @Override
            public void onFailure(Call<PopularProducts> call, Throwable t) {
                Log.d("Error", t.getMessage());
                hideProgressDialog();

            }
        });


           }

}
