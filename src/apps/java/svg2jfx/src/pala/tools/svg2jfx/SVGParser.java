package pala.tools.svg2jfx;

import java.text.DecimalFormat;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import pala.libs.generic.parsing.BufferedParser;
import pala.libs.generic.parsing.CharacterParser;
import pala.libs.generic.parsing.Parser;

public class SVGParser extends BufferedParser<PathElement> {

	private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.################################");

	private String varname = "p";
	private boolean started;

	public String getVarname() {
		return varname;
	}

	public void setVarname(String varname) {
		this.varname = varname;
	}

	private final CharacterParser in;

	public SVGParser(CharacterParser in) {
		this.in = in;
	}

	public SVGParser(Parser<Character> in) {
		this(CharacterParser.from(in));
	}

	private char previousCmd;

	private PathElement parseNext(int chr) {

		if (!started) {
			chr = Character.toUpperCase(chr);
			started = true;
		}
		final int c = chr;

		switch (c) {
		case -1:
			return null;
		case 'M':
		case 'm': {

			in.parseWhitespace();
			double val = parseValue();
			parseSeparator();
			double val2 = parseValue();

			String v1 = DECIMAL_FORMATTER.format(val), v2 = DECIMAL_FORMATTER.format(val2);
			return new PathElement() {
				@Override
				public String toSVGPart() {
					return (char) c + v1 + "," + v2;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'm'
							? "{\n\tMoveTo moveTo = new MoveTo(" + v1 + ", " + v2
									+ ");\n\tmoveTo.setAbsolute(false);\n\t" + varname + ".add(moveTo);\n}"
							: varname + ".add(new MoveTo(" + v1 + ", " + v2 + "));";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					MoveTo m = new MoveTo(val, val2);
					if (c == 'M')
						m.setAbsolute(true);
					return m;
				}
			};
		}

		case 'L':
		case 'l': {
			in.parseWhitespace();
			double val = parseValue();
			parseSeparator();
			double val2 = parseValue();

			String v1 = DECIMAL_FORMATTER.format(val), v2 = DECIMAL_FORMATTER.format(val2);
			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + v1 + ',' + v2;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'l'
							? "{\n\tLineTo lineTo = new LineTo(" + v1 + ", " + v2
									+ ");\n\tlineTo.setAbsolute(false);\n\t" + varname + ".add(lineTo);\n}"
							: varname + ".add(new LineTo(" + v1 + ", " + v2 + "));";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					LineTo lt = new LineTo(val, val2);
					if (c == 'L')
						lt.setAbsolute(true);
					return lt;
				}
			};
		}

		case 'H':
		case 'h': {
			in.parseWhitespace();
			double val = parseValue();

			String v = DECIMAL_FORMATTER.format(val);
			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + v;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'h'
							? "{\n\tHLineTo hLineTo = new HLineTo(" + v + ");\n\thLineTo.setAbsolute(false);\n\t"
									+ varname + ".add(hLineTo);\n}"
							: varname + ".add(new HLineTo(" + v + "));";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					HLineTo hlt = new HLineTo(val);
					if (c == 'H')
						hlt.setAbsolute(true);
					return hlt;
				}
			};
		}

		case 'V':
		case 'v': {
			in.parseWhitespace();
			double val = parseValue();

			String v = DECIMAL_FORMATTER.format(val);
			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + v;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'v'
							? "{\n\tVLineTo vLineTo = new VLineTo(" + v + ");\n\tvLineTo.setAbsolute(false);\n\t"
									+ varname + ".add(vLineTo);\n}"
							: varname + ".add(new VLineTo(" + v + "));";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					HLineTo hlt = new HLineTo(val);
					if (c == 'V')
						hlt.setAbsolute(true);
					return hlt;
				}
			};
		}

		case 'Z':
		case 'z': {

			return new PathElement() {

				@Override
				public String toSVGPart() {
					return "Z";
				}

				@Override
				public String toJavaFXStatement() {
					return varname + ".add(new ClosePath());";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					return new ClosePath();
				}
			};
		}

		case 'C':
		case 'c': {
			in.parseWhitespace();
			double x1 = parseValue();
			parseSeparator();
			double y1 = parseValue();
			parseSeparator();
			double x2 = parseValue();
			parseSeparator();
			double y2 = parseValue();
			parseSeparator();
			double x3 = parseValue();
			parseSeparator();
			double y3 = parseValue();

			String sx1 = DECIMAL_FORMATTER.format(x1), sx2 = DECIMAL_FORMATTER.format(x2), sx3 = DECIMAL_FORMATTER.format(x3), sy1 = DECIMAL_FORMATTER.format(y1),
					sy2 = DECIMAL_FORMATTER.format(y2), sy3 = DECIMAL_FORMATTER.format(y3);

			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + sx1 + ',' + sy1 + ' ' + sx2 + ',' + sy2 + ' ' + sx3 + ',' + sy3;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'c'
							? "{\n\tCubicCurveTo cubicCurveTo = new CubicCurveTo(" + sx1 + ", " + sy1 + ", " + sx2
									+ ", " + sy2 + ", " + sx3 + " " + sy3 + ");\n\tcubicCurveTo.setAbsolute(false);\n\t"
									+ varname + ".add(cubicCurveTo);\n}"
							: varname + ".add(new CubicCurveTo(" + sx1 + ", " + sy1 + ", " + sx2 + ", " + sy2 + ", "
									+ sx3 + ", " + sy3 + ");";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					CubicCurveTo cc2 = new CubicCurveTo(x1, y1, x2, y2, x3, y3);
					if (c == 'C')
						cc2.setAbsolute(true);
					return cc2;
				}
			};
		}

		case 'Q':
		case 'q': {
			in.parseWhitespace();
			double x1 = parseValue();
			parseSeparator();
			double y1 = parseValue();
			parseSeparator();
			double x2 = parseValue();
			parseSeparator();
			double y2 = parseValue();

			String sx1 = DECIMAL_FORMATTER.format(x1), sx2 = DECIMAL_FORMATTER.format(x2), sy1 = DECIMAL_FORMATTER.format(y1), sy2 = DECIMAL_FORMATTER.format(y2);

			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + sx1 + ',' + sy1 + ' ' + sx2 + ',' + sy2;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'q'
							? "{\n\tQuadCurveTo quadCurveTo = new QuadCurveTo(" + sx1 + ", " + sy1 + ", " + sx2 + ", "
									+ sy2 + ");\n\tquadCurveTo.setAbsolute(false);\n\t" + varname
									+ ".add(quadCurveTo);\n}"
							: varname + ".add(new QuadCurveTo(" + sx1 + ", " + sy1 + ", " + sx2 + ", " + sy2 + ");";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					QuadCurveTo cc2 = new QuadCurveTo(x1, y1, x2, y2);
					if (c == 'q')
						cc2.setAbsolute(true);
					return cc2;
				}
			};
		}

		case 'A':
		case 'a': {
			in.parseWhitespace();
			double rx = parseValue();
			parseSeparator();
			double ry = parseValue();
			parseSeparator();
			double xar = parseValue();
			parseSeparator();
			int laf = (int) parseValue();
			parseSeparator();
			int sf = (int) parseValue();
			parseSeparator();
			double x = parseValue();
			parseSeparator();
			double y = parseValue();
			parseSeparator();

			String rxs = DECIMAL_FORMATTER.format(rx), rys = DECIMAL_FORMATTER.format(ry), xars = DECIMAL_FORMATTER.format(xar), lafs = String.valueOf(laf == 1),
					sfs = String.valueOf(sf == 1), xs = DECIMAL_FORMATTER.format(x), ys = DECIMAL_FORMATTER.format(y);

			return new PathElement() {

				@Override
				public String toSVGPart() {
					return (char) c + rxs + ' ' + rys + ' ' + xars + ' ' + lafs + ' ' + sfs + ' ' + xs + ' ' + ys;
				}

				@Override
				public String toJavaFXStatement() {
					return c == 'a'
							? "{\n\tArcTo arcTo = new ArcTo(" + rxs + ", " + rys + ", " + xars + ", " + xs + ", " + ys
									+ ", " + lafs + ", " + sfs + ");\n\tarcTo.setAbsolute(false);\n\t" + varname
									+ ".add(arcTo);\n}"
							: varname + ".add(new ArcTo(" + rxs + ", " + rys + ", " + xars + ", " + xs + ", " + ys
									+ ", " + lafs + ", " + sfs + "));";
				}

				@Override
				public javafx.scene.shape.PathElement toJavaFX() {
					return new ArcTo(rx, ry, xar, x, y, laf == 1, sf == 1);
				}
			};
		}

		default:
			throw new IllegalArgumentException("Unexpected value: " + (char) c);
		}
	}

	/**
	 * Parses the next {@link PathElement}, returning <code>null</code> if the end
	 * of input has been reached or an error if there is otherwise malformed input.
	 */
	@Override
	protected PathElement read() {
		in.parseWhitespace();
		int c = in.pk();
		return c == -1 ? null
				: Character.isDigit(c) || c == '-' || c == '.' ? parseNext(previousCmd)
						: parseNext(previousCmd = in.next());

	}

	private void parseSeparator() {
		in.parseWhitespace();
		if (in.pk() == ',') {
			in.nxt();
			in.parseWhitespace();
		}
	}

	private double parseValue() {
		in.parseWhitespace();
		String str = "";
		if (in.pk() == '-') {
			str += '-';
			in.nxt();
		}
		str += in.clct(Character::isDigit);
		if (in.pk() == '.') {
			str += '.';
			in.nxt();
			str += in.clct(Character::isDigit);
		}

		if (in.pk() == 'e') {
			in.next();
			str += 'e';
			if (in.pk() == '-') {
				in.next();
				str += '-';
			}
			str += in.clct(Character::isDigit);
		}

		if (str.isEmpty())
			throw new RuntimeException("Malformed input to SVGParser.");
		return Double.parseDouble(str);

	}

}
