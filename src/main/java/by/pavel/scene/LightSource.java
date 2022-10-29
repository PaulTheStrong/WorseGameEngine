package by.pavel.scene;

import by.pavel.math.Vector3f;
import by.pavel.math.Vector4f;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LightSource {

    private Vector4f color;
    private Vector3f position;
    private float diffuseIntensity;
    private float specularIntensity;

}
