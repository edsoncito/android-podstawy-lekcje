package pl.javastart.ap.webclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import pl.javastart.ap.R;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WebclientActivity extends Activity implements NewCategoryCallback, FinishedDownloadingCatetegoriesCallback {

    private Spinner categorySpinner;
    private ArrayAdapter<Category> categoryAdapter;
    private TextView log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webclient);
        log = (TextView) findViewById(R.id.log);
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        categoryAdapter = new ArrayAdapter<>(WebclientActivity.this, android.R.layout.simple_dropdown_item_1line);
        categorySpinner.setAdapter(categoryAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webclient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            addNewCategoryPressed();
            return true;
        }

        if (item.getItemId() == R.id.refresh) {
            refreshCategories();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshCategoriesRetrofit() {
        Util.appendToLog(log, "Pobieranie danych za pomocą Retrofit");
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://webservice-javastartpl.rhcloud.com/")
                .build();

        CategoryRetrofitService categoryService = restAdapter.create(CategoryRetrofitService.class);

        categoryService.getAll(new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
                Util.appendToLog(log, "Pobrano. Odświeżanie spinnera...");
                onFinishedDownloadingCategories(categories);
            }

            @Override
            public void failure(RetrofitError error) {
                // ignore
            }
        });
    }

    private void refreshCategories() {
        refreshCategoriesRetrofit();
        // old fasion way
        // new DownloadCategoriesAsyncTask(this, log).execute();
    }


    private void addNewCategoryPressed() {
        NewCategoryFragment fragment = new NewCategoryFragment();
        fragment.show(getFragmentManager(), "newCategoryFragment");
    }

    @Override
    public void newCategoryAddButtonPressed(String name) {
        Category category = new Category(name);
        new NewCategoryAsyncTask(WebclientActivity.this, log).execute(category);
    }

    @Override
    public void onFinishedDownloadingCategories(List<Category> categories) {
        categoryAdapter.clear();
        categoryAdapter.addAll(categories);
        categorySpinner.invalidate();
        Util.appendToLog(log, "Spinner odźwieżony.");
    }

    public void categoryDeleteButtonPressed(View view) {
        if(categorySpinner.getSelectedItem() == null) {
            return;
        }

        Category category = (Category) categorySpinner.getSelectedItem();

        Util.appendToLog(log, "Usuwanie kategorii " + category.getName());
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://webservice-javastartpl.rhcloud.com/")
                .build();

        CategoryRetrofitService categoryService = restAdapter.create(CategoryRetrofitService.class);

        categoryService.delete(categorySpinner.getSelectedItemId(), new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Util.appendToLog(log, "Usunięto.");
            }

            @Override
            public void failure(RetrofitError error) {
                Util.appendToLog(log, "Coś poszło nie tak podczas usuwania.");
            }
        });
    }
}
