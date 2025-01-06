import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

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

  record Number(Long value) implements Token {
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
    } catch (Exception e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  static void tokenize(String input) throws Exception {
    int current = 0;

    while (current < input.length()) {
      char currentChar = input.charAt(current);

      switch (currentChar) {
        case ('{') -> {
        }
        case ('}') -> {
        }
        case ('[') -> {
        }
        case (']') -> {
        }
        case ('"') -> {
        }
        case '-' | '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' -> {
        }
        case (',') -> {
        }
        case (':') -> {
        }
        case ('t') -> {
        }
        case ('f') -> {
        }
        case ('n') -> {
        }
        default -> {
        }
      }
    }
  }
}
