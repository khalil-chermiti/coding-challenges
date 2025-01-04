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

record JsonObject() {
}

public class Jezmol {

  public static void main(String[] args) {
    try {
      String content = Files.readString(Path.of(args[0]));
      if (parse(tokenize(content)) == null) {
        System.out.println("1");
      } else {
        System.out.println("0");
      }
    } catch (Exception e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  static List<Token> tokenize(String input) throws Exception {
    List<Token> tokens = new ArrayList<>();
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

    return tokens;
  }

  public static JsonObject parse(List<Token> tokens) throws Exception {
    int currentToken = 0;

    while (currentToken < tokens.size()) {
      Token token = tokens.get(currentToken++);

      return switch (token) {
        case Token.LeftBrace _ -> {
          // handle kv properties : if StringToken : curToken++ should be col | }
          // string : string , string : string ,

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
    }

    return null;
  }
}