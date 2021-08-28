package com.devyb.Devybqrlogo;


import android.content.Context;
import android.graphics.*;
import android.graphics.Canvas;
import android.widget.ImageView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


@DesignerComponent(version = 2,
        description = "<p>This extension allows you to generate QR codes with logo in the middle.</p>\n" +
                "<p>Developed by <a href=\"https://community.kodular.io/u/devyb\" rel=\"noopener noreferrer\" target=\"_blank\">DevYb</a></p>",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://res.cloudinary.com/dujfnjfcz/image/upload/v1596225104/icon16.png")
@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "zxing-3.3.1.jar")
@SimpleObject(external = true)
public class DevYbQRLogo extends AndroidNonvisibleComponent {
    private Context context;
    private ComponentContainer container;

    private String text = "Hello World!";
    private int backgroundColor = COLOR_WHITE;
    private int color = COLOR_BLACK;
    private int size = 300;
    private String charset = "UTF-8";
    private String logoPath = "";


    public DevYbQRLogo(ComponentContainer container) {
        super(container.$form());
        this.container = container;

        context = container.$context();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Hello World!")
    @SimpleProperty
    public void Text(String text) {
        this.text = text;
    }

    @SimpleProperty
    public String Text() {
        return text;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void Color(int color) {
        this.color = color;
    }

    @SimpleProperty
    public int Color() {
        return color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "300")
    @SimpleProperty
    public void Size(int size) {
        this.size = size;
    }

    @SimpleProperty
    public int Size() {
        return size;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "UTF-8")
    @SimpleProperty
    public void Charset(String charset) {
        this.charset = charset;
    }

    @SimpleProperty
    public String Charset() {
        return charset;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    @SimpleProperty
    public void Logo(String path) {
        final String tempPath = (path == null) ? "" : path;

        if (MediaUtil.isExternalFile(tempPath)) {
            if (form.isDeniedPermission(READ_EXTERNAL_STORAGE)) {
                Error("permission denied");
            }else{
                logoPath = path;
            }
        } else {
            if (container.$form() instanceof ReplForm) {
                if (context.getPackageName().equalsIgnoreCase("io.makeroid.companion")) {
                    logoPath = "/storage/emulated/0/Makeroid/assets/" + path;
                }else if (context.getPackageName().equalsIgnoreCase("com.niotron.companion")){
                    logoPath = "/storage/emulated/0/Android/data/com.niotron.companion/files/assets/" + path;
                }else{
                    logoPath = "/storage/emulated/0/Android/data/edu.mit.appinventor.aicompanion3/files/assets/" + path;
                }
            } else
                logoPath = path;
        }
    }

    @SimpleProperty
    public String Logo() {
        return logoPath;
    }

    private void CreateQRCode(String text, String charset, int qrCodeheight, int qrCodewidth, Image image, int color, int backgroundColor, boolean saveFile, String fileDir) {

        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(new String(text.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? color : backgroundColor;
                }
            }
            //creating bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            //getting the logo
            Bitmap overlay = null;

            if (MediaUtil.isExternalFile(logoPath)) {
                overlay = BitmapFactory.decodeFile(logoPath);
            } else {
                if (container.$form() instanceof ReplForm)
                    overlay = BitmapFactory.decodeFile(logoPath);
                else {
                    InputStream inputStream = form.openAsset(logoPath);
                    overlay = BitmapFactory.decodeStream(inputStream);
                }
            }

            //setting bitmap to image view
            if (saveFile) {
                saveBitmap(mergeBitmaps(overlay, bitmap), fileDir);
            } else {
                ImageView imageView = (ImageView) image.getView();
                imageView.setImageBitmap(mergeBitmaps(overlay, bitmap));
            }
        } catch (Exception ex) {
            Error(ex.toString());
        }
    }


    private Bitmap mergeBitmaps(Bitmap logo, Bitmap qrcode) {

        Bitmap combined = Bitmap.createBitmap(qrcode.getWidth(), qrcode.getHeight(), qrcode.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        canvas.drawBitmap(qrcode, new Matrix(), null);

        Bitmap resizeLogo = Bitmap.createScaledBitmap(logo, canvasWidth / 5, canvasHeight / 5, true);
        int centreX = (canvasWidth - resizeLogo.getWidth()) / 2;
        int centreY = (canvasHeight - resizeLogo.getHeight()) / 2;
        canvas.drawBitmap(resizeLogo, centreX, centreY, null);
        return combined;
    }

    @SimpleFunction
    public void Generate(Image imageComponent) {

        try {
            int width = size, height = size;
            int smallestDimension = width < height ? width : height;

            CreateQRCode(text, charset, smallestDimension, smallestDimension, imageComponent, color, backgroundColor, false, null);

        } catch (Exception ex) {
            Error(ex.toString());
        }
    }

    @SimpleFunction
    public void GenerateAndSave(String output) {

        try {
            int width = size, height = size;
            int smallestDimension = width < height ? width : height;

            CreateQRCode(text, charset, smallestDimension, smallestDimension, null, color, backgroundColor, true, output);

        } catch (Exception ex) {
            Error(ex.toString());
        }
    }

    @SimpleEvent
    public void Error(String error) {
        EventDispatcher.dispatchEvent(this, "Error", error);
    }


    private void saveBitmap(Bitmap bitmap, String path) {
        if (bitmap != null) {
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (Exception e) {
                    Error(e.getMessage());
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Error(e.toString());
            }
        }
    }

}
