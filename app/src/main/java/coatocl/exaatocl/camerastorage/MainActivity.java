package coatocl.exaatocl.camerastorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private static final int REQUEST = 1;
    FloatingActionButton button;
    Button buttonInternal,buttonGallery;
    OutputStream outputStream = null;
    RecyclerView recycler;
    ArrayList<String> imageList;
    Adapter adapter;
    Bitmap bitmap;
    String IMAGES_FOLDER_NAME = "DIGAL-Camera";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        buttonInternal = findViewById(R.id.buttonInternal);
        buttonGallery = findViewById(R.id.buttonGallery);

        recycler = findViewById(R.id.recycler);
        imageList = new ArrayList<>();

            if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            
        button.setOnClickListener(v ->
        {

            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},1);
            }
            else
            {
                Intent intent023 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent023,REQUEST);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(gridLayoutManager);
        adapter = new Adapter(this,imageList);
        recycler.setAdapter(adapter);
        getImagePath();

        buttonInternal.setOnClickListener(v -> {
            Intent intentGo = new Intent(this, InternalStorage.class);
            startActivity(intentGo);
        });

        buttonGallery.setOnClickListener(v -> {
            Intent intentGo2 = new Intent(this,Gallery.class);
            startActivity(intentGo2);
        });

    }

    private void getImagePath()
    {
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Audio.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";

        @SuppressLint("InlinedApi")
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[] {"DIGAL-Camera"};

        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);
        assert cursor != null;
        int count = cursor.getCount();
        for (int i = 0; i < count; i++)
        {

            cursor.moveToPosition(i);

            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            imageList.add(cursor.getString(dataColumnIndex));
        }
        adapter.notifyDataSetChanged();
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission granted for Access camera", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Give Permission for Access camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST && resultCode == RESULT_OK && data != null){
            Bundle bundle = data.getExtras();
            assert bundle != null;
            bitmap = (Bitmap) bundle.get("data");
        }

        if(button.isClickable())
        {
            getImageList();
        }

        else
        {
            Toast.makeText(this, "error...", Toast.LENGTH_SHORT).show();
        }

        }

    private void getImageList()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                assert imageUri != null;
                outputStream = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
           bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "Image Saved!" +IMAGES_FOLDER_NAME , Toast.LENGTH_SHORT).show();
            try {
                assert outputStream != null;
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

            File file = new File(imagesDir);

            boolean make = file.mkdir();
            if (!make)
            {
                Toast.makeText(this, "Successful..." , Toast.LENGTH_SHORT).show();
            }

            File image = new File(imagesDir, System.currentTimeMillis() + ".jpg");

            try {
                outputStream = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "Image Saved!" + image , Toast.LENGTH_SHORT).show();
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

