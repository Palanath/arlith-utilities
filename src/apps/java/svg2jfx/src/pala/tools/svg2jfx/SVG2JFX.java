package pala.tools.svg2jfx;

import pala.libs.generic.parsing.CharacterParser;

public class SVG2JFX {

	public static void main(String[] args) {
		if (args.length == 1) {
			SVGParser p = new SVGParser(CharacterParser.from(args[0]));
			PathElement pe;
			while ((pe = p.next()) != null)
				System.out.println(pe.toJavaFXStatement());
		} else
			System.out.println("Invalid number of arguments. Please provide the SVG Path as a single argument.");
	}

}
