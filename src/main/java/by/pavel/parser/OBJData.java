package by.pavel.parser;

import by.pavel.math.Vector2f;
import by.pavel.math.Vector3f;
import by.pavel.math.Vector3i;
import by.pavel.math.Vector4f;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class OBJData {
    private final List<Vector3f> vertices;
    private final List<Vector2f> textures;
    private final List<Vector3f> normals;
    private final List<Vector4f> vertexParams;
    private final List<List<Vector3i>> surfaces;
}
