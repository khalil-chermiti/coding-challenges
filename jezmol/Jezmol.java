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

  record Column() implements Token {
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

// object , array , string , number , boolean , null
// pair(k,v)
sealed interface JezmolObject {
  record Null() implements JezmolObject {
  }

  record Boolean() implements JezmolObject {
  }

  record number(Long value) implements JezmolObject {
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

  public static void main(String[] args) {
    try {
      String content = Files.readString(Path.of(args[0]));
      tokenize(content);
      tokens.stream().forEach(token -> System.out.println("Token : " + token.getClass().getSimpleName()));
    } catch (Exception e) {
      System.out.println(e.getMessage());
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
        case (':') -> tokens.add(new Token.Column());

        case ('t') -> {
          if (safeLookahead(input, current, 3).equals("rue")) {
            tokens.add(new Token.BooleanToken(true));
            current += 3;
          } else {
            throw new Exception("invalid token when parsing boolean token");
          }
        }

        case ('f') -> {
          if (safeLookahead(input, current, 4).equals("alse")) {
            tokens.add(new Token.BooleanToken(false));
            current += 4;
          } else {
            throw new Exception("invalid token when parsing boolean token");
          }
        }

        case ('n') -> {
          if (safeLookahead(input, current, 3).equals("ull")) {
            tokens.add(new Token.Null());
            current += 3;
          } else {
            throw new Exception("invalid token when parsing null token");
          }
        }

        case ('"') -> {
          StringBuilder string = new StringBuilder();
          while (safeCharAt(input, current) != '"') {
            string.append(safeCharAt(input, current));
            current++;
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

            // edge case : -.6
            if (currentChar == '-' && !Character.isDigit(safeCharAt(input, current)))
              throw new Exception("Invalid number format, expect digit after: -");

            while (Character.isDigit(safeCharAt(input, current)) || safeCharAt(input, current) == '.') {
              number.append(safeCharAt(input, current));
              current++;
            }

            // edge case : 543.
            if (safeCharAt(input, current - 1) == '.')
              throw new Exception("Invalid number format, number can't end with: .");
            tokens.add(new Token.Number(Double.parseDouble(number.toString())));

            continue;
          }

          throw new Exception("Invalid token: " + currentChar);
        }
      }
    }
  }

  static char safeCharAt(String input, int index) throws Exception {
    if (index >= input.length()) {
      throw new Exception("Unexpected end of input while accessing character.");
    }
    return input.charAt(index);
  }

  static String safeLookahead(String input, int current, int lookaheadLength) throws Exception {
    if (current + lookaheadLength > input.length()) {
      throw new Exception("Unexpected end of input while looking ahead.");
    }
    return input.substring(current, current + lookaheadLength);
  }
}
