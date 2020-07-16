package com.vis.scrayon

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import androidx.core.view.get
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var mImageBtnCurrentPaint: ImageButton? = null

    companion object {
        private const val GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        mImageBtnCurrentPaint = llPaint[1] as ImageButton
        mImageBtnCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallete_selected
            )
        )
        drawingView.setBrushSize(20.toFloat())
        ib_brush.setOnClickListener {
            setBrushDialog()
        }
        ib_undo.setOnClickListener {
            drawingView.onClickUndo()
        }
        ib_redo.setOnClickListener({
            drawingView.onClickRedo()
        })
        ib_choose_photo.setOnClickListener({
            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, GALLERY)
        })
        ib_save.setOnClickListener{
            var filename=runOnBackground(getBitmap(container_view))
            if(filename.isEmpty()){
               // Toast.makeText(this,"Error Occured.Please try after Sometime",Toast.LENGTH_SHORT).show()
                val snackbar = Snackbar
                    .make(it, "Error Occured.Please try after Sometime", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        this,
                        R.color.Red
                    )
                )
                snackbar.show()

            }else{
                val snackbar: Snackbar = Snackbar
                    .make(it, "File Saved Successfully", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_green_dark
                    )
                )
                snackbar.show()
                //Toast.makeText(this,"File Created $filename",Toast.LENGTH_SHORT).show()
                ib_share.visibility=View.VISIBLE
                ib_share.setOnClickListener{
                    MediaScannerConnection.scanFile(this, arrayOf(filename), null) { path, uri ->
                        val shareIntent = Intent()
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        shareIntent.type = "image/png"
                        startActivity(
                            Intent.createChooser(shareIntent, "Share")
                        )
                    }
                }
            }

        }
    }

    private  fun runOnBackground(bitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()

        bitmap!!.compress(Bitmap.CompressFormat.PNG, 90, bytes)
        val f =
            File(
                externalCacheDir!!.absoluteFile.toString()
                        + File.separator + "CrayonDraw_"
                        + System.currentTimeMillis() / 1000 + ".png"
            )

        val fos = FileOutputStream(f)
        fos.write(bytes.toByteArray())
        fos.close()
        return f.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        imageView.setImageURI(data.data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private  fun getBitmap(view: View): Bitmap {
        val returnBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }

    private fun setBrushDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size ")
        val verysmallbrush = brushDialog.ib_very_small_brush
        verysmallbrush.setOnClickListener({
            drawingView.setBrushSize(5.toFloat())
            brushDialog.dismiss()
        })

        val smallbrush = brushDialog.ib_small_brush
        smallbrush.setOnClickListener({
            drawingView.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumbrush = brushDialog.ib_medium_brush

        mediumbrush.setOnClickListener({
            drawingView.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        })
        val largebrush = brushDialog.ib_large_brush

        largebrush.setOnClickListener({
            drawingView.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }


    fun paintClicked(view: View) {
        if (view != mImageBtnCurrentPaint) {
            val imageButton = view as ImageButton
            val color = imageButton.getTag().toString()
            drawingView.setColor(color)
            imageButton!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallete_selected
                )
            )
            mImageBtnCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallete_normal
                )
            )
            mImageBtnCurrentPaint = view

        }
    }
}
