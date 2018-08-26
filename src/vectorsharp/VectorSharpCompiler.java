package vectorsharp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VectorSharpCompiler {
	public static enum Token {

		UNKNOWN(-1), LINE_END(0),

		PLUS(20), MINUS(21), MULTIPLY(22), DIVIDE(23), LENGTH(24), SQUARE_ROOT(25), EQUALS(26), NORMALIZE_START(27), NORMALIZE_END(28), EXPONENT(29),

		VARIABLE_DEF(30),

		IDENTIFIER(1), NUMBER(2), COMMA(3), OPEN_VECTOR(4), CLOSE_VECTOR(5), OPEN_PARENTHESE(6), CLOSE_PARENTHESE(7), PRINT(8), PERIOD(9);

		public int id;

		Token(int i) {
			id = i;
		}
	}

	private static HashMap<Token, Integer> opPrecedence = new HashMap<>();

	static {
		opPrecedence.put(Token.MINUS, 2);
		opPrecedence.put(Token.PLUS, 4);
		opPrecedence.put(Token.DIVIDE, 6);
		opPrecedence.put(Token.MULTIPLY, 8);
		opPrecedence.put(Token.EXPONENT, 10);
	}

	private static int charIndex = -1;
	private static String[] code;
	private static int lineNum = 0;

	private static String identifierString;
	private static double numVal;

	private static HashMap<String, Object> variables;
	private static List<AbstractSyntaxToken> syntaxTokens;

	private static Token curTok;
	
	private static StringBuilder output;

	public static String interpret(String codeString) {
		charIndex = -1;
		lineNum = 0;
		variables = new HashMap<>();
		output = new StringBuilder();
		syntaxTokens = new ArrayList<>();
		codeString = codeString.replace("\r", "");
		code = codeString.split("\n");
		getTok();
		while (true) {
			int prevLine = lineNum;
			// The only possible top level tokens are variable definitions,
			// variable changes, print statements, and empty lines
			switch (curTok) {
			case LINE_END:
				charIndex = -1;
				lineNum++;
				if (lineNum < code.length) {
					getTok();
					break;
				}
				return output.toString();
			case VARIABLE_DEF:
				HandleVariableDef();
				break;
			case PRINT:
				HandlePrint();
				break;
			default:
				HandleVariableAssign();
				break;
			}
			if (prevLine == lineNum && curTok != Token.LINE_END) {
				break;
			}
		}
		return output.toString();
	}

	private static void HandleVariableDef() {
		Object o;
		switch (identifierString) {
		case "vector":
			o = new Vector(0, 0, 0);
			break;
		case "double":
			o = 0.0d;
			break;
		case "int":
			o = (int) 0;
			break;
		default:
			return;
		}
		getTok(); // eat variable type
		variables.put(identifierString, o);
		HandleVariableAssign();
	}

	private static void HandleVariableAssign() {
		if (!variables.containsKey(identifierString)) {
			logErr("Could not find variable '" + identifierString + "'");
			return;
		}
		String var = identifierString;
		getTok(); // Eat variable name
		if (curTok != Token.EQUALS)
			return;
		getTok(); // Eat equals sign
		variables.put(var, evaluateExpr(parseExpr()));
	}

	private static void HandlePrint() {
		getTok(); // Eat print
		output.append(evaluateExpr(parseExpr()) + "\n");
	}

	private static void getTok() {
		curTok = getNextToken();
	}

	private static int getTokPrecedence() {
		return opPrecedence.containsKey(curTok) ? opPrecedence.get(curTok) : -1;
	}

	private static AbstractSyntaxToken parseNumberExpr() {
		AbstractSyntaxToken a = new AbstractSyntaxToken.NumberExprToken(numVal);
		getTok(); // Eat number
		return a;
	}

	private static AbstractSyntaxToken parseParentheseExpr() {
		getTok();// Eat (
		AbstractSyntaxToken a = parseExpr();
		if (curTok != Token.CLOSE_PARENTHESE) {
			logErr("Expected a ')'");
			return null;
		}
		getTok(); // Eat )
		return a;
	}

	private static AbstractSyntaxToken parseIdentifierExpr() {
		if (variables.containsKey(identifierString)) {
			String id = identifierString;
			getTok();// Eat variable name
			AbstractSyntaxToken.VariableExprToken variableExpr = new AbstractSyntaxToken.VariableExprToken(id);
			if (curTok == Token.PERIOD) {
				getTok(); // Eat period
				if (curTok != Token.IDENTIFIER) {
					logErr("Expected identifier after '.'");
					return null;
				}
				String subfield = identifierString;
				getTok();// Eat subfield
				return new AbstractSyntaxToken.SubFieldExprToken(identifierString, variableExpr);
			}
			return variableExpr;
		}
		logErr("Could not find variable '" + identifierString + "'");
		return null;
	}

	private static AbstractSyntaxToken parseVectorExpr() {
		getTok();// Eat <
		AbstractSyntaxToken arg1 = parseExpr();
		if (curTok != Token.COMMA) {
			logErr("Expected a ','");
			return null;
		}
		getTok(); // Eat comma
		AbstractSyntaxToken arg2 = parseExpr();
		if (curTok == Token.CLOSE_VECTOR) {
			// 2D Vector here
			getTok(); // Eat >
			Object o1 = evaluateExpr(arg1);
			Object o2 = evaluateExpr(arg2);
			if (o1 instanceof Double && o2 instanceof Double) {
				Vector v = Vector.fromAngleMagnitude((double)o1, (double)o2);
				return new AbstractSyntaxToken.VectorExprToken(v);
			}
			logErr("Vector args must be numerical values");
			return null;
		} else if (curTok == Token.COMMA) {
			// Must be 3D Vector
			getTok();// Eat comma
			AbstractSyntaxToken arg3 = parseExpr();
			if (curTok != Token.CLOSE_VECTOR) {
				logErr("Expected a '>'");
				return null;
			}
			getTok();// Eat >
			Object o1 = evaluateExpr(arg1);
			Object o2 = evaluateExpr(arg2);
			Object o3 = evaluateExpr(arg3);
			if (o1 instanceof Double && o2 instanceof Double && o3 instanceof Double) {
				Vector v = new Vector((double) o1, (double) o2, (double) o3);
				return new AbstractSyntaxToken.VectorExprToken(v);
			}
			logErr("Vector args must be numerical values");
			return null;
		}
		logErr("Expected a ',' or a '>'");
		return null;
	}

	private static AbstractSyntaxToken parseVectorLengthExpr() {
		getTok(); // Eat |
		AbstractSyntaxToken a = parseExpr();
		Object value = evaluateExpr(a);
		if (value instanceof Vector) {
			if (curTok != Token.LENGTH) {
				logErr("Expected '|'");
				return null;
			}
			getTok(); // Eat |
			return new AbstractSyntaxToken.NumberExprToken(((Vector) value).getMagnitude());
		}
		logErr("Expected vector after '|'");
		return null;
	}

	private static AbstractSyntaxToken parseSqrtExpr() {
		getTok(); // Eat ~
		AbstractSyntaxToken a = parseExpr();
		Object value = evaluateExpr(a);
		if (value instanceof Double) {
			if (curTok != Token.SQUARE_ROOT) {
				logErr("Expected '~'");
				return null;
			}
			getTok(); // Eat ~
			return new AbstractSyntaxToken.NumberExprToken(Math.sqrt((double) value));
		}
		logErr("Expected number after '~'");
		return null;
	}

	private static AbstractSyntaxToken parseNormalizeExpr() {
		getTok(); // Eat [
		AbstractSyntaxToken a = parseExpr();
		Object value = evaluateExpr(a);
		if (value instanceof Vector) {
			if (curTok != Token.NORMALIZE_END) {
				logErr("Expected ']'");
				return null;
			}
			getTok(); // Eat ]
			return new AbstractSyntaxToken.VectorExprToken(((Vector) value).normalize());
		}
		logErr("Expected vector after '['");
		return null;
	}

	private static AbstractSyntaxToken parsePrimary() {
		// THIS SHOULD EAT WHATEVER TOKEN IT IS CURRENTLY ON
		switch (curTok) {
		default:
			logErr("Unexpected token: " + curTok);
			return null;
		case NUMBER:
			return parseNumberExpr();
		case MINUS:
			getTok(); // Eat minus
			return new AbstractSyntaxToken.BinaryOpToken(Token.MULTIPLY, new AbstractSyntaxToken.NumberExprToken(-1), parsePrimary());
		case IDENTIFIER:
			return parseIdentifierExpr();
		case OPEN_PARENTHESE:
			return parseParentheseExpr();
		case OPEN_VECTOR:
			return parseVectorExpr();
		case LENGTH:
			return parseVectorLengthExpr();
		case NORMALIZE_START:
			return parseNormalizeExpr();
		case SQUARE_ROOT:
			return parseSqrtExpr();
		}
	}

	private static AbstractSyntaxToken parseExpr() {
		// THIS SHOULD ALSO EAT THE ENTIRE EXPRESSION
		AbstractSyntaxToken lhs = parsePrimary();
		if (lhs == null)
			return null;
		return parseOp(0, lhs);
	}

	private static AbstractSyntaxToken parseOp(int precendence, AbstractSyntaxToken lhs) {
		while (true) {
			int tokPrec = getTokPrecedence();
			if (tokPrec < precendence)
				return lhs;
			Token op = curTok;
			getTok(); // Eat op
			if(op == Token.EXPONENT && curTok != Token.OPEN_PARENTHESE){
				logErr("Expected '(' after '^'");
				return null;
			}
			AbstractSyntaxToken rhs = parsePrimary();// Eats rhs
			if (tokPrec < getTokPrecedence()) {
				rhs = parseOp(tokPrec + 1, rhs);
			}

			lhs = new AbstractSyntaxToken.BinaryOpToken(op, lhs, rhs);
		}
	}

	private static void logErr(String s) {
		output.append("ERROR AT LINE " + lineNum + ": " + s + "\n");
	}

	private static Token getNextToken() {
		String line = code[lineNum];
		charIndex++;
		if (charIndex >= line.length())
			return Token.LINE_END;
		char startingChar = line.charAt(charIndex);
		while (startingChar == ' ' || startingChar == '\n') {
			if (charIndex + 1 >= line.length())
				return Token.LINE_END;
			startingChar = line.charAt(++charIndex);
		}
		if (startingChar == '#')
			return Token.LINE_END;
		if (Character.isLetter(startingChar)) {
			identifierString = startingChar + "";
			while ((charIndex + 1) < line.length() && Character.isLetterOrDigit(line.charAt(charIndex + 1))) {
				identifierString += line.charAt(++charIndex);
			}
			if (identifierString.equals("vector"))
				return Token.VARIABLE_DEF;
			if (identifierString.equals("double"))
				return Token.VARIABLE_DEF;
			if (identifierString.equals("print"))
				return Token.PRINT;
			return Token.IDENTIFIER;
		} else if (Character.isDigit(startingChar)) {
			String number = startingChar + "";
			while ((charIndex + 1) < line.length() && (Character.isDigit(line.charAt(charIndex + 1)) || line.charAt(charIndex + 1) == '.')) {
				number += line.charAt(++charIndex);
			}
			numVal = Double.parseDouble(number);
			return Token.NUMBER;
		} else if (startingChar == ',')
			return Token.COMMA;
		else if (startingChar == '<')
			return Token.OPEN_VECTOR;
		else if (startingChar == '>')
			return Token.CLOSE_VECTOR;
		else if (startingChar == '=')
			return Token.EQUALS;
		else if (startingChar == '+')
			return Token.PLUS;
		else if (startingChar == '-')
			return Token.MINUS;
		else if (startingChar == '*')
			return Token.MULTIPLY;
		else if (startingChar == '/')
			return Token.DIVIDE;
		else if (startingChar == '|')
			return Token.LENGTH;
		else if (startingChar == '(')
			return Token.OPEN_PARENTHESE;
		else if (startingChar == ')')
			return Token.CLOSE_PARENTHESE;
		else if (startingChar == '~')
			return Token.SQUARE_ROOT;
		else if (startingChar == '[')
			return Token.NORMALIZE_START;
		else if (startingChar == ']')
			return Token.NORMALIZE_END;
		else if (startingChar == '.')
			return Token.PERIOD;
		else if (startingChar == '^')
			return Token.EXPONENT;
		return Token.UNKNOWN;
	}

	private static Object evaluateExpr(AbstractSyntaxToken a) {
		if (a instanceof AbstractSyntaxToken.NumberExprToken) {
			return ((AbstractSyntaxToken.NumberExprToken) a).value;
		} else if (a instanceof AbstractSyntaxToken.VariableExprToken) {
			return variables.get(((AbstractSyntaxToken.VariableExprToken) a).value);
		} else if (a instanceof AbstractSyntaxToken.VectorExprToken) {
			return ((AbstractSyntaxToken.VectorExprToken) a).value;
		} else if (a instanceof AbstractSyntaxToken.BinaryOpToken) {
			AbstractSyntaxToken.BinaryOpToken bOp = ((AbstractSyntaxToken.BinaryOpToken) a);
			switch (bOp.value) {
			case PLUS:
				return add(evaluateExpr(bOp.lhs), evaluateExpr(bOp.rhs));
			case MINUS:
				return subtract(evaluateExpr(bOp.lhs), evaluateExpr(bOp.rhs));
			case MULTIPLY:
				return multiply(evaluateExpr(bOp.lhs), evaluateExpr(bOp.rhs));
			case DIVIDE:
				return divide(evaluateExpr(bOp.lhs), evaluateExpr(bOp.rhs));
			case EXPONENT:
				return power(evaluateExpr(bOp.lhs), evaluateExpr(bOp.rhs));
			default:
				return null;
			}
		} else if (a instanceof AbstractSyntaxToken.SubFieldExprToken) {
			AbstractSyntaxToken.SubFieldExprToken subfieldExpr = ((AbstractSyntaxToken.SubFieldExprToken) a);
			Object variableValue = evaluateExpr(subfieldExpr.lhs);
			if (variableValue instanceof Vector) {
				Vector v = (Vector) variableValue;
				switch (subfieldExpr.subfield) {
				case "x":
					return v.x;
				case "y":
					return v.y;
				case "z":
					return v.z;
				}
			}
			logErr(subfieldExpr.lhs.value + " contains no subfield named '" + subfieldExpr.subfield + "'");
			return null;
		}
		return null;
	}

	private static Object add(Object o1, Object o2) {
		if (o1 instanceof Vector && o2 instanceof Vector) {
			return ((Vector) o1).add((Vector) o2);
		}
		if (o1 instanceof Double && o2 instanceof Double) {
			return (double) o1 + (double) o2;
		}
		logErr("Could not add " + o1 + " and " + o2);
		return null;
	}

	private static Object subtract(Object o1, Object o2) {
		if (o1 instanceof Vector && o2 instanceof Vector) {
			return ((Vector) o1).subtract((Vector) o2);
		}
		if (o1 instanceof Double && o2 instanceof Double) {
			return (double) o1 - (double) o2;
		}
		logErr("Could not subtract " + o2 + " from " + o1);
		return null;
	}

	private static Object multiply(Object o1, Object o2) {
		if (o1 instanceof Vector && o2 instanceof Double) {
			double d2 = (double) o2;
			return ((Vector) o1).multiply(d2);
		}
		if (o2 instanceof Vector && o1 instanceof Double) {
			double d1 = (double) o1;
			return ((Vector) o2).multiply(d1);
		}
		if (o1 instanceof Double && o2 instanceof Double) {
			double d2 = (double) o2;
			return (double) o1 * d2;
		}
		logErr("Could not multiply " + o1 + " and " + o2);
		return null;
	}

	private static Object divide(Object o1, Object o2) {
		if (o1 instanceof Vector && o2 instanceof Double) {
			double d2 = (double) o2;
			if (d2 == 0) {
				logErr("Division by zero");
				return null;
			}
			return ((Vector) o1).divide(d2);
		}
		if (o1 instanceof Double && o2 instanceof Double) {
			double d2 = (double) o2;
			if (d2 == 0) {
				logErr("Division by zero");
				return null;
			}
			return (double) o1 / d2;
		}
		logErr("Could not divide " + o1 + " and " + o2);
		return null;
	}
	
	private static Object power(Object o1, Object o2){
		if (o1 instanceof Double && o2 instanceof Double) {
			double d2 = (double) o2;
			return Math.pow((double)o1, d2);
		}
		logErr("Could not raise " + o1 + " to the " + o2 + " power");
		return null;
	}
}
