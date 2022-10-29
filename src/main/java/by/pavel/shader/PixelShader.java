package by.pavel.shader;

import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;

public interface PixelShader {
    Vector4f getPixelColor(Vector3f cameraPosition, Vector3f pixelNormal, Vector3f pixelModelPosition, Vector4f pixelColor);
}
