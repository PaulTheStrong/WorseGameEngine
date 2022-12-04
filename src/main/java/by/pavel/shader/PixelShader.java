package by.pavel.shader;

import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import by.pavel.scene.Model;

public interface PixelShader {
    Vector4f getPixelColor(Vector3f cameraPosition, PixelData pixelData);
}
