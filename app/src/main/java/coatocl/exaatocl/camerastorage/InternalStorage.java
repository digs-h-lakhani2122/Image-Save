package coatocl.exaatocl.camerastorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class InternalStorage extends AppCompatActivity
{
    Button buttonExternal2,buttonGallery2;
    private static final int REQUEST = 1;
    FloatingActionButton button2;
    RecyclerView recycler2;
    ArrayList<String> imageList;
    Adapter adapter2;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_storage);

        buttonExternal2 = findViewById(R.id.buttonExternal2);
        buttonGallery2 = findViewById(R.id.buttonGallery2);

        button2 = findViewById(R.id.button2);

        recycler2 = findViewById(R.id.recycler2);
        imageList = new ArrayList<>();

        button2.setOnClickListener(v ->
        {

            if(ActivityCompat.checkSelfPermission(InternalStorage.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(InternalStorage.this,new String[]{Manifest.permission.CAMERA},1);
            }
            else
            {
                Intent intent223 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent223,REQUEST);
            }
        });

        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this, 3);
        recycler2.setLayoutManager(gridLayoutManager2);
        adapter2 = new Adapter(this,imageList);
        recycler2.setAdapter(adapter2);
        getImageFromStorage();

        buttonExternal2.setOnClickListener(v -> {
            Intent intentGo = new Intent(this,MainActivity.class);
            startActivity(intentGo);
        });

        buttonGallery2.setOnClickListener(v -> {
            Intent intentGo2 = new Intent(this,Gallery.class);
            startActivity(intentGo2);
        });

    }

    private void getImageFromStorage()
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("D-image", Context.MODE_PRIVATE);
//        File file = new File(directory, System.currentTimeMillis() + ".jpg");
        File[] files = directory.listFiles();

        assert files != null;
        for (File file2 : files)
        {
            imageList.add(file2.getAbsolutePath());
        }

        adapter2.notifyDataSetChanged();
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

        if(button2.isClickable())
        {
//
            getImageFromInternalStorage();
        }

        else
        {
            Toast.makeText(this, "error...", Toast.LENGTH_SHORT).show();
        }

    }

    private void getImageFromInternalStorage()
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("D-image", Context.MODE_PRIVATE);
        File file = new File(directory, System.currentTimeMillis() + ".jpg");
        if (!file.exists())
        {
            Log.d("path", file.toString());
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(this, "Image Saved to internal Storage!", Toast.LENGTH_SHORT).show();
                fos.flush();
                fos.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
