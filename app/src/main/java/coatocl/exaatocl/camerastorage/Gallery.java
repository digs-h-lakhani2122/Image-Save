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
import java.util.ArrayList;

public class Gallery extends AppCompatActivity
{

    Button buttonExternal1, buttonInternal1;
    private static final int REQUEST = 1;
    FloatingActionButton button1;
    RecyclerView recycler1;
    ArrayList<String> imageList;
    Adapter adapter;
    static Bitmap bitmap;
    static FileOutputStream outputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        buttonExternal1 = findViewById(R.id.buttonExternal1);
        buttonInternal1 = findViewById(R.id.buttonInternal1);

        button1 = findViewById(R.id.button1);

        recycler1 = findViewById(R.id.recycler1);
        imageList = new ArrayList<>();

        button1.setOnClickListener(v ->
        {

            if (ActivityCompat.checkSelfPermission(Gallery.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Intent intent123 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent123, REQUEST);
            }
        });

        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(this, 3);
        recycler1.setLayoutManager(gridLayoutManager1);
        adapter = new Adapter(this, imageList);
        recycler1.setAdapter(adapter);
        getImagePath();

        buttonExternal1.setOnClickListener(v -> {
            Intent intentGo = new Intent(this, MainActivity.class);
            startActivity(intentGo);
        });

        buttonInternal1.setOnClickListener(v -> {
            Intent intentGo2 = new Intent(this, InternalStorage.class);
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
            String[] selectionArgs = new String[] {Environment.DIRECTORY_DCIM};

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String [] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted for Access camera", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Give Permission for Access camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            assert bundle != null;
            bitmap = (Bitmap) bundle.get("data");
        }

        if (button1.isClickable())
        {
            getImageFromGallery();
        }
        else
            {
            Toast.makeText(this, "error...", Toast.LENGTH_SHORT).show();
        }

    }

    private void getImageFromGallery()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM );
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                assert imageUri != null;
                outputStream = (FileOutputStream) resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "Image Saved to!" + Environment.DIRECTORY_DCIM , Toast.LENGTH_SHORT).show();
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
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

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
