import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

sealed interface Token
    permits Token.LeftBrace, Token.RightBrace, Token.StringToken, Token.ColumnToken, Token.CommaToken {
  record LeftBrace() implements Token {
  }

  record RightBrace() implements Token {
  }

  record StringToken(String value) implements Token {
  }

  record ColumnToken() implements Token {
  }

  record CommaToken() implements Token {
  }
}

record JsonObject(List<Pair> pairs) {
}

record Pair(String key, Object value) {
}

public class Jezmol {
  static List<Token> tokens = new ArrayList<>();

  public static void main(String[] args) {
    try {
      String content = Files.readString(Path.of(args[0]));
      tokenize(content);
      if (parse() == null) {
        System.out.println("1");
      } else {
        System.out.println("0");
      }
    } catch (Exception e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  static void tokenize(String input) throws Exception {
    int current = 0;

    // loop through input
    while (current < input.length()) {
      char c = input.charAt(current++);

      if (c == '{') {
        tokens.add(new Token.LeftBrace());
      }
      if (c == '}') {
        tokens.add(new Token.RightBrace());
      }
      if (c == ':') {
        tokens.add(new Token.ColumnToken());
      }
      if (c == ',') {
        tokens.add(new Token.CommaToken());
      }
      if (c == ' ') {
        continue;
      }
      if (c == '"') {
        StringBuilder sb = new StringBuilder();

        while (current < input.length() && input.charAt(current) != '"')
          sb.append(input.charAt(current++));

        if (current == input.length() || input.charAt(current) != '"')
          throw new Exception("unterminated string or EOF");

        current++; // consume second "

        // if " construct string from sb and return Token.StringToken(sb.toString)
        tokens.add(new Token.StringToken(sb.toString()));
      }

    }

    // parse valid tokens
  }

  public static void parseValue() {
  }

  static int currentToken = 0;

  public static Object parse() throws Exception {

    Token token = tokens.get(currentToken++);

    return switch (token) {
      case Token.LeftBrace lb -> {
        JsonObject jo = new JsonObject(new ArrayList<>());
        // handle kv properties : if StringToken : curToken++ should be col | }
        // string : string , string : string ,

        while (!(tokens.get(currentToken) instanceof Token.RightBrace)) {
          if(currentToken >= tokens.size()){
            throw new Exception("EOF exception");
          }

          // test for key (must be of string type)
          if (tokens.get(currentToken) instanceof  Token.StringToken) {
            Token.StringToken key = (Token.StringToken) tokens.get(currentToken);
            currentToken++; // consume the key
            // test for column
            if (!(tokens.get(currentToken) instanceof Token.ColumnToken))
              throw new Exception("expected a key of type string");
            currentToken++;

            // test for value recursively
            var value = parse();

            // test comma
            if (tokens.get(currentToken) instanceof Token.CommaToken) {
              currentToken++;
            }

          } else {
            throw new Exception("expected a key of type string");
          }

        }

        if (!(tokens.get(currentToken) instanceof Token.RightBrace)) {
          throw new Exception("expected right brace");
        }
        currentToken++;
        yield new JsonObject();
      }
      default -> {
        throw new Exception("unexpected token");
      }
    };

    return null;
  }
}