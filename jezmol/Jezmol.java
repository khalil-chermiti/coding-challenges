import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

sealed interface Token {
  record LeftBrace() implements Token {
  }

  record RightBrace() implements Token {
  }

  record LeftBracket() implements Token {
  }

  record RightBracket() implements Token {
  }

  record Colon() implements Token {
  }

  record Comma() implements Token {
  }

  record StringToken(String value) implements Token {
  }

  record Null() implements Token {
  }

  record BooleanToken(Boolean value) implements Token {
  }

  record Number(Double value) implements Token {
  }
}

sealed interface JezmolObject {
  record Null() implements JezmolObject {
  }

  record Boolean(boolean value) implements JezmolObject {
  }

  record Number(Double value) implements JezmolObject {
  }

  record StringObject(String value) implements JezmolObject {
  }

  record Pair(StringObject key, JezmolObject value) implements JezmolObject {
  }

  record Array(List<JezmolObject> elements) implements JezmolObject {
  }

  record Object(List<Pair> props) implements JezmolObject {
  }
}

public class Jezmol {
  static List<Token> tokens = new ArrayList<>();
  private static int tokenIndex = 0;

  public static void main(String[] args) {
    try {
      String content = Files.readString(Path.of(args[0]));
      tokenize(content);
      JezmolObject parsedObject = parse();
      System.out.println("Parsed JSON object: " + parsedObject);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  static void tokenize(String input) throws Exception {
    int current = 0;

    while (current < input.length()) {
      char currentChar = safeCharAt(input, current++);

      switch (currentChar) {
        case ('{') -> tokens.add(new Token.LeftBrace());
        case ('}') -> tokens.add(new Token.RightBrace());
        case ('[') -> tokens.add(new Token.LeftBracket());
        case (']') -> tokens.add(new Token.RightBracket());
        case (',') -> tokens.add(new Token.Comma());
        case (':') -> tokens.add(new Token.Colon());

        case ('t') -> {
          if (safeLookahead(input, current, 3).equals("rue")) {
            tokens.add(new Token.BooleanToken(true));
            current += 3;
          } else {
            throw new Exception("Invalid token when parsing boolean token");
          }
        }

        case ('f') -> {
          if (safeLookahead(input, current, 4).equals("alse")) {
            tokens.add(new Token.BooleanToken(false));
            current += 4;
          } else {
            throw new Exception("Invalid token when parsing boolean token");
          }
        }

        case ('n') -> {
          if (safeLookahead(input, current, 3).equals("ull")) {
            tokens.add(new Token.Null());
            current += 3;
          } else {
            throw new Exception("Invalid token when parsing null token");
          }
        }

        case ('"') -> {
          StringBuilder string = new StringBuilder();
          while (safeCharAt(input, current) != '"') {
            string.append(safeCharAt(input, current++));
          }
          tokens.add(new Token.StringToken(string.toString()));
          current++; // consume closing quote
        }

        default -> {
          if (Character.isWhitespace(currentChar)) {
            continue;
          }

          if (Character.isDigit(currentChar) || currentChar == '-') {
            StringBuilder number = new StringBuilder();
            number.append(currentChar);

            if (currentChar == '-' && !Character.isDigit(safeCharAt(input, current))) {
              throw new Exception("Invalid number format, expect digit after: -");
            }

            while (Character.isDigit(safeCharAt(input, current)) || safeCharAt(input, current) == '.') {
              number.append(safeCharAt(input, current++));
            }

            if (number.charAt(number.length() - 1) == '.') {
              throw new Exception("Invalid number format, number can't end with: .");
            }

            tokens.add(new Token.Number(Double.parseDouble(number.toString())));
            continue;
          }

          throw new Exception("Invalid token: " + currentChar);
        }
      }
    }
  }

  static JezmolObject parse() throws Exception {
    return parseValue();
  }

  private static JezmolObject parseValue() throws Exception {
    Token token = advance();
    return switch (token) {
      case Token.StringToken st -> new JezmolObject.StringObject(st.value());
      case Token.BooleanToken bt -> new JezmolObject.Boolean(bt.value());
      case Token.Number nt -> new JezmolObject.Number(nt.value());
      case Token.Null _ -> new JezmolObject.Null();
      case Token.LeftBrace _ -> parseObject();
      case Token.LeftBracket _ -> parseArray();
      default -> throw new Exception("Unexpected Token: " + token.getClass().getSimpleName());
    };
  }

  private static JezmolObject parseObject() throws Exception {
    JezmolObject.Object object = new JezmolObject.Object(new ArrayList<>());

    while (!(peek() instanceof Token.RightBrace)) {
      if (!(advance() instanceof Token.StringToken keyToken)) {
        throw new Exception("Expected a string token as key in JSON object");
      }

      if (!(advance() instanceof Token.Colon)) {
        throw new Exception("Expected ':' after key in JSON object");
      }

      JezmolObject value = parseValue();
      object.props().add(new JezmolObject.Pair(new JezmolObject.StringObject(keyToken.value()), value));

      if (peek() instanceof Token.Comma) {
        advance(); // Consume the comma
      } else if (!(peek() instanceof Token.RightBrace)) {
        throw new Exception("Expected ',' or '}' in JSON object");
      }
    }
    advance(); // Consume the closing brace
    return object;
  }

  private static JezmolObject parseArray() throws Exception {
    JezmolObject.Array array = new JezmolObject.Array(new ArrayList<>());

    while (!(peek() instanceof Token.RightBracket)) {
      array.elements().add(parseValue());

      if (peek() instanceof Token.Comma) {
        advance(); // Consume the comma
      } else if (!(peek() instanceof Token.RightBracket)) {
        throw new Exception("Expected ',' or ']' in JSON array");
      }
    }
    advance(); // Consume the closing bracket
    return array;
  }

  private static char safeCharAt(String input, int index) throws Exception {
    if (index >= input.length()) {
      throw new Exception("Unexpected end of input while accessing character.");
    }
    return input.charAt(index);
  }

  private static String safeLookahead(String input, int current, int lookaheadLength) throws Exception {
    if (current + lookaheadLength > input.length()) {
      throw new Exception("Unexpected end of input while looking ahead.");
    }
    return input.substring(current, current + lookaheadLength);
  }

  private static Token advance() throws Exception {
    if (tokenIndex >= tokens.size()) {
      throw new Exception("Unexpected end of tokens while parsing JSON");
    }
    return tokens.get(tokenIndex++);
  }

  private static Token peek() throws Exception {
    if (tokenIndex >= tokens.size()) {
      throw new Exception("Unexpected end of tokens while parsing JSON");
    }
    return tokens.get(tokenIndex);
  }
}
