package pala.tools.requestcreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class RequestCreator {

	private static final Scanner s = new Scanner(System.in);

	public static void main(String[] args) {
		var requestName = promptRequestName();
		var responseType = promptResponseType();
		var fields = promptFields();
		var errors = promptExceptions();
		var classname = className(requestName);

		// Package declaration
		System.out.println("\n\n");
		System.out.println("package pala.apps.arlith.api.communication.protocol.requests;\n");

		// Imports
		System.out.println(
				"import pala.libs.generic.JSONObject;\nimport pala.libs.generic.json.JSONValue;\n\nimport pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;\nimport pala.apps.arlith.api.connections.scp.CommunicationConnection;\nimport pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;");
		for (var v : fields)
			System.out.println("import pala.apps.arlith.api.communication.protocol.types." + v[0] + ';');
		for (var v : errors)
			System.out.println("import pala.apps.arlith.api.communication.protocol.errors." + v + ';');
		System.out.println("import pala.apps.arlith.api.communication.protocol.types." + responseType + ';');

		System.out.println();

		// Class header
		System.out.println(
				"public class " + classname + " extends SimpleCommunicationProtocolRequest<" + responseType + "> {");

		// Constants
		System.out.println("\tpublic static final String REQUEST_NAME = \"" + requestName(requestName) + "\";\n");

		if (!fields.isEmpty())
			System.out.println("\tprivate static final String " + generateFieldKeyDeclarationList(fields) + ";\n");

		for (var v : fields)
			System.out.println("\tprivate " + v[0] + ' ' + v[1] + ';');

		System.out.println();

		// Constructors
		System.out.println("\tpublic " + classname + '(' + generateConstructorParameterList(fields) + ") {");
		System.out.println("\t\tsuper(REQUEST_NAME);");
		for (var v : fields)
			System.out.println("\t\tthis." + v[1] + " = " + v[1] + ';');
		System.out.println("\t}");

		System.out.println();

		System.out.println("\tpublic " + classname + "(JSONObject properties) {\n\t\tsuper(REQUEST_NAME, properties);");
		for (var v : fields)
			System.out.println("\t\t" + v[1] + " = new " + v[0] + "(properties.get(" + v[2] + "));");
		System.out.println("\t}");

		// Fields, getters, and setters
		for (var v : fields) {
			System.out.println(
					"\n\tpublic " + v[0] + " get" + Character.toUpperCase(v[1].charAt(0)) + v[1].substring(1) + "() {");
			System.out.println("\t\treturn " + v[1] + ';');
			System.out.println("\t}");
			System.out.println();
			System.out.println("\tpublic " + classname + " set" + Character.toUpperCase(v[1].charAt(0))
					+ v[1].substring(1) + '(' + v[0] + ' ' + v[1] + ") {");
			System.out.println("\t\tthis." + v[1] + " = " + v[1] + ';');
			System.out.println("\t\treturn this;");
			System.out.println("\t}");
		}

		System.out.println();

		// Implementation methods
		System.out.println("\t@Override");
		System.out.println("\tprotected void build(JSONObject object) {");
		for (var v : fields)
			System.out.println("\t\tobject.put(" + v[2] + ", " + v[1] + ".json());");
		System.out.println("\t}");

		System.out.println();

		System.out.println("\t@Override");
		System.out.println("\tpublic " + responseType + " parseReturnValue(JSONValue json) {");
		System.out.println("\t\treturn new " + responseType + "(json);");
		System.out.println("\t}");

		System.out.println();
		System.out.println("\t@Override");
		System.out.println("\tpublic " + responseType
				+ " receiveResponse(CommunicationConnection client) throws IllegalCommunicationProtocolException"
				+ (errors.length != 0 ? ", " + String.join(", ", errors) : "") + " {");
		System.out.println("\t\ttry {");
		System.out.println("\t\t\treturn super.receiveResponse(client);");
		System.out.println("\t\t} catch (" + String.join(" | ", errors) + " e) {");
		System.out.println("\t\t\tthrow e;");
		System.out.println("\t\t} catch (CommunicationProtocolError e) {");
		System.out.println("\t\t\tthrow new IllegalCommunicationProtocolException(e);");
		System.out.println("\t\t}");
		System.out.println("\t}");

		System.out.println('}');

	}

	private static String generateFieldKeyDeclarationList(List<String[]> fields) {
		List<String> decls = new ArrayList<>();
		for (var v : fields) {
			var parts = parsePartsFromCamelCaseName(v[1]);
			String content = '"' + getKeyValue(parts) + '"';
			String fieldName = v[2];
			decls.add(fieldName + " = " + content);
		}
		return String.join(", ", decls.toArray(new String[decls.size()]));
	}

	private static String requestName(String[] requestName) {
		return String.join("-", requestName);
	}

	private static String separator(char c) {
		StringBuilder sb = new StringBuilder("\n-");
		for (int i = 0; i < 50; i++)
			sb.append(c).append('-');
		return sb.append('\n').toString();
	}

	private static void printSep(char c) {
		System.out.println(separator(c));
	}

	private static String[] promptRequestName() {
		while (true) {
			printSep('1');
			System.out.print(
					"Please enter the request name, with normal word separation.\n\tExamples: [get email], [set profile icon], [create account], [get user ID]\n\tInfo: This is used to derive the ClassName, request-name, and other properties that will go in the class (like the constructor).\n\tName: ");
			String input = s.nextLine();
			if (input.isEmpty())
				continue;
			String[] res = splitToWords(input);
			return res;
		}
	}

	private static String promptResponseType() {
		printSep('2');
		System.out.print(
				"Enter the response type as a Java type. The trailing \"Value\" can be ommitted. If nothing is entered, \"CompletionValue\" (the void type) is assumed.\n\tExamples: [CompletionValue], [TextValue], [], [Completion], [List<TextValue>]\n\tType: ");
		var val = s.nextLine().trim();
		if (val.isEmpty())
			val = "CompletionValue";
		else if (!val.toLowerCase().endsWith("value"))
			val += "Value";
		return val;
	}

	/**
	 * Returns a {@link List} of {@link String} arrays. Each array represents one
	 * class property (or field). The first element in the array is the type of the
	 * field, e.g. <code>TextValue</code>. The second element is the field name,
	 * e.g. <code>name</code>, or <code>userID</code>. The third is the name of the
	 * key constant corresponding to that field, e.g. <code>NAME_KEY</code> or
	 * <code>USER_ID_KEY</code>.
	 * 
	 * @return A list of all the fields.
	 */
	private static List<String[]> promptFields() {
		printSep('3');
		System.out.println(
				"Please enter each field, preceded by its type.\n\tExamples: [TextValue name], [GIDValue userID], [ListValue<TextValue> users], [Text name], [List<TextValue> users]\n\tInfo: This is used to populate the class with fields and create the getters and setters for the class.\n\tShortcuts: The \"Value\" portion can be left out of the first type specified per field. For example, if you enter \"List<TextValue>\", it becomes \"ListValue<TextValue>\".\n\tType nothing and hit enter to finish.");
		List<String[]> names = new ArrayList<>();
		while (true) {
			System.out.print("\tField: ");
			String nl = s.nextLine();
			if (nl.isEmpty())
				return names;
			var sp = splitToWords(nl);
			if (sp.length != 2) {
				System.out.print("The field must have a type and a name.\n\tField: ");
				continue;
			}
			if (!sp[0].toLowerCase().endsWith("value"))
				sp[0] += "Value";

			names.add(new String[] { sp[0], sp[1], getKeyName(parsePartsFromCamelCaseName(sp[1])) });
		}
	}

	private static String[] promptExceptions() {
		printSep('4');
		System.out.println(
				"Enter the name of each Error that the request may produce.\n\tSpecial: Enter a '-' (hyphen) to exclude the default errors: [Restricted] and [Syntax].\n\tExamples: [Restricted], [Syntax], [ObjectNotFound]");
		Set<String> strs = new HashSet<>();
		String str;
		boolean incdef = true;
		while (!(str = s.nextLine().trim()).isBlank()) {
			if (str.equals("-"))
				if (!incdef)
					System.err.println("Default errors have already been excluded!");
				else
					incdef = false;
			else
				strs.add(str + (str.endsWith("Error") ? "" : "Error"));
		}
		if (incdef) {
			strs.add("RestrictedError");
			strs.add("SyntaxError");
		}
		return strs.toArray(new String[strs.size()]);
	}

	private static String[] splitToWords(String in) {
		in = in.trim();
		while (in.contains("  "))
			in = in.replace("  ", " ");
		return in.split(" ");
	}

	private static String className(String[] pieces) {
		String[] str = Arrays.copyOf(pieces, pieces.length);
		for (int i = 0; i < str.length; i++)
			str[i] = Character.toUpperCase(str[i].charAt(0)) + str[i].substring(1);
		return String.join("", str) + "Request";
	}

	private static String generateConstructorParameterList(List<String[]> params) {
		List<String> l = new ArrayList<>(params.size());
		for (var v : params)
			l.add(v[0] + ' ' + v[1]);
		return String.join(", ", l.toArray(new String[l.size()]));
	}

	private static List<String> parsePartsFromCamelCaseName(String name) {
		List<String> parts = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		for (char c : name.toCharArray())
			if (Character.isUpperCase(c)) {
				if (sb.isEmpty())
					sb.append(c);
				else if (Character.isUpperCase(sb.charAt(sb.length() - 1)))
					sb.append(c);
				else {
					parts.add(sb.toString().toLowerCase());
					sb = new StringBuilder().append(c);
				}
			} else if (sb.isEmpty())
				sb.append(Character.toUpperCase(c));
			else if (Character.isUpperCase(sb.charAt(sb.length() - 1))) {
				// The previous character is upper case. This could mean one of two things.
				// We're either in a scenario where we have one upper case letter, and we're
				// parsing a follower letter to that, e.g.:
				// User
				// where we're at the s, or we could have something like this:
				// IDField
				// where we're at the i. In the former case, we're good to keep parsing. In the
				// latter case, we need to separate the "ID" portion as its own part; only the
				// last capital letter, "F" in the above case, is a part of the part we're
				// currently parsing.
				//
				// First let's check for these scenarios.
				if (sb.length() == 1)
					// This is the first case.
					sb.append(c);
				else {
					// The length is at least 2.
					var fp = sb.substring(0, sb.length() - 1);// All but the last char.
					parts.add(fp);
					sb.delete(0, sb.length() - 1);// Remove all but last char.
					sb.append(c);
				}
			} else
				sb.append(c);// Append like normal.
		// After looping, we should always have some leftover chars (unless the name is
		// empty).
		parts.add(sb.toString().toLowerCase());
		return parts;
	}

	/**
	 * Returns a key name, e.g. <code>USER_ID_KEY</code>, from the parts of the
	 * field name, e.g. <code>["user", "id"]</code>. The capitalization of the parts
	 * does not matter when calling this method.
	 * 
	 * @param nameParts The parts of the name of the field to get the key from.
	 * @return The name of the key for the field represented by the
	 *         <code>nameParts</code> provided.
	 */
	private static String getKeyName(List<String> nameParts) {
		var cl = nameParts.toArray(new String[nameParts.size()]);
		for (int i = 0; i < cl.length; i++)
			cl[i] = cl[i].toUpperCase();

		return String.join("_", cl) + "_KEY";
	}

	/**
	 * Returns the key value for the field represented by the provided field name
	 * parts. The parts are expected to be <b>lowercase</b>.
	 * 
	 * @param nameParts The parts of the name of the field to get the value from.
	 *                  These must all be lowercase.
	 * @return The value of the key for the field represented by the
	 *         <code>nameParts</code> provided.
	 */
	private static String getKeyValue(List<String> nameParts) {
		return String.join("-", nameParts);
	}

}
