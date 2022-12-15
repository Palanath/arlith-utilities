package pala.tools.svg2jfx;

public interface PathElement {
	String toJavaFXStatement();

	String toSVGPart();

	javafx.scene.shape.PathElement toJavaFX();
}
