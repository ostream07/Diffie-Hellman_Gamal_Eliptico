//package ssi.gamaleliptico;

//**************************************************************************************//
// Asignatura: Seguridad en Sistemas Informáticos                                       //
// Práctica Práctica 10. Diffie-Hellman y ElGamal Elípticos                             //
// Autor: Saúl Pérez García                                                             //
// Correo: alu0101129785@ull.edu.es                                                     //
// Fichero: Implementación de Diffie-Hellman y ElGamal Elípticos            		//
//                                                                                      //
// Método de ejecución: javac GamalEliptico.java                                        //
//                      java GamalEliptico      					//
//**************************************************************************************//

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class GamalEliptico {

  private static class Point {
    int x;
    int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
  
  // Function to check Lehmann's test 
  private static boolean lehmann(int n, int t) {
    if (n % 2 == 0) {
      return false;
    }
    int[] smallPrimes = {2, 3, 5, 7, 11};
    for (int i = 0; i < smallPrimes.length; i++) {
      if (n > smallPrimes[i] && n % smallPrimes[i] == 0) {
        return false;
      }
    }
    
    // create instance of Random class 
    Random rand = new Random();
    // generating a random base less than n 
    int a = rand.nextInt(n - 3) + 2;
    // calculating exponent 
    int e = (n - 1) / 2;
    // iterate to check for different base values 
    // for given number of tries 't' 
    while (t > 0) {
      // calculating final value using formula 
      int result = (powER(a, e, n));

      // if not equal, try for different base 
      if ((result % n) == 1 || (result % n) == (n - 1)) {
        a = rand.nextInt(n - 3) + 2;
        t -= 1;
      } // else return negative 
      else {
        return false;
      }
    }
    // return positive after attempting 
    return true;
  }
  
  // Rapid exponenciation. Iterative Function to calculate (x^y) in O(log y)
  static int powER(int b, int a, int m) {
    long x = 1; // Initialize result
    long power = b % m;
 
    if (x == 0)
      return 0; // In case x is divisible by p;
 
    while (a > 0) {
 
      // If y is odd, multiply x with result
      if ((a & 1) != 0)
        x = (x * power) % m;
 
      // y must be even now
      a = a / 2; // y = y/2
      power = (power * power) % m;
      //System.out.println(x + " Power : " + power );
    }
    return (int) x;
  }
  
  // Calculates module
  private static int mod(int n, int p) {
    if (n < 0) {
      return (n % p) + p;
    } else {
      return (n % p);
    }
  }
  
    //  Extended Euclides, return array [d, a, b] such that d = gcd(p, q), ap + bq = d
  private static int[] gcd(int p, int q) {
    if (q == 0) {
      return new int[]{p, 1, 0};
    }

    int[] values = gcd(q, p % q);
    int d = values[0];
    int a = values[2];
    int b = values[1] - (p / q) * values[2];
    return new int[]{d, a, b};
  }
  
  // Polinomio de la curva  elíptica
  private static int eliptic(int x, int a, int b) {
    return x*x*x + a*x + b;
  }
  
  // Cálculo de los puntos de la curva
  private static Point[] curvPoints(int a, int b, int p) {
    int[] xValues = new int[p];
    int[] yValues = new int[p];
    
    for(int i = 0; i < p; i++) {
      xValues[i] = mod(eliptic(i, a, b), p);
      
      yValues[i] = mod(i*i, p);
    }
    ArrayList<Point> points = new ArrayList<>();
    for(int x = 0; x < p; x++) {
      for(int y = 0; y < p; y++) {
        if(xValues[x] == yValues[y]){
          // añadimos a la lista
          points.add(new Point(x, y));
        }
      }
    }
    // Conversión a array
    return points.toArray(new Point[0]);
  }
  
  // Cálculo de lambda
  private static int lambda(int a, int p, Point A, Point B) {
    int num, den;
    if(A.x == B.x && A.y == B.y) {
      num = (3 * A.x * A.x) + a;
      den = (2 * A.y);
    } else {
      num = B.y - A.y;
      den = B.x - A.x;
    }
    //System.out.println("num: " + num);
    //System.out.println("den: " + den);
    int[] gcd = gcd(p, den);
    int invDen = mod(gcd[2], p);
    int lambda = mod(num * invDen, p);
    //System.out.println("lambda: " + lambda);
    return lambda;
  }
  
  // Suma dos puntos de la curva
  private static Point pointAdd(int a, int p, Point A, Point B) {
    //System.out.println("A: " + A.x + ", " + A.y);
    //System.out.println("b: " + B.x + ", " + B.y);
    int lambda = lambda(a, p, A, B);
    int x3 = mod(((lambda * lambda) - A.x - B.x), p);
    int y3 = mod((lambda * (A.x - x3) - A.y), p);
    //System.out.println("x3: " + x3 + "/ " + y3);
    return new Point(x3, y3);
  }
  
  // Calcula las potencias de 2 por P --> [P, 2P, 4P, 8P, ...]
  private static Point[] pointPowers(int a, int p, int power, Point point) {
    Point[] powers = new Point[power];
    powers[0] = point;
    for(int i = 1; i < power; i++) {
      powers[i] = pointAdd(a, p, powers[i-1], powers[i-1]);
    }
    return powers;
  }
  
  // Multiplica un escalar por Point
  private static Point multiplyPoint(int a, int p, int n, Point point) {
    String binary = Integer.toBinaryString(n);
    //System.out.println("binary: " + binary);
    Point[] powers = pointPowers(a, p, binary.length(), point);
    Point result = null;
    for(int i = 0; i < binary.length(); i++) {
      if(binary.charAt(i) == '1') {
        if(result == null) {
          result = powers[binary.length() - 1 - i];
        } else { 
          result = pointAdd(a, p, result, powers[binary.length() - 1 - i]);
        }
      }
    }
    return result;
  }
  
  // Cálculo de la M
  private static int computeM(int m) {
    int M = 2;
    while(!(M > m)) {
      M *= 2;
    }
    return M;
  }
  
  // Busca en la lista de puntos si alguno tiene en su coordenada x, el valor x
  private static Point findPointCoordX(Point[] points, int x) {
    for(int i = 0; i < points.length; i++) {
      if(points[i].x == x) {
        return points[i];
      }
    }
    return null;
  }
  
  // Halla el punto cuya coordenada x tenga el j menor
  private static Point findPoint(Point[] points, int m, int h) {
    int mh = m * h;
    for(int j = 0; j < h; j++) {
      int x = mh + j;
      Point pointCoded = findPointCoordX(points, x);
      if(pointCoded != null) {
        return pointCoded;
      }
    }
    return null;
  }
  
  private static void elipticGamal(int p, int a, int b, Point G, int dB, int dA, int m) {
    Point[] curvPoints = curvPoints(a, b, p);
    
    //System.out.println("db: " + dB);
    Point dBG = multiplyPoint(a, p, dB, G);
    
    //System.out.println("da: " + dA);
    Point dAG = multiplyPoint(a, p, dA, G);
    
    Point secretA = multiplyPoint(a, p, dA, dBG);
    Point secretB = multiplyPoint(a, p, dB, dAG);
    
    int M = computeM(m);
    
    int h = p/M;
    
    Point Qm = findPoint(curvPoints, m, h);
    
    Point cipherMessage = pointAdd(a, p, Qm, secretA);
    
    System.out.println("Puntos de la curva: ");
    for (Point res : curvPoints) {
      System.out.println("( " + res.x + ", " + res.y + " )");
    }
    System.out.println("");
    System.out.println("dBG: " + dBG.x + ", " + dBG.y);
    System.out.println("dAG: " + dAG.x + ", " + dAG.y);
    System.out.println("secretA: " + secretA.x + ", " + secretA.y);
    System.out.println("secretB: " + secretB.x + ", " + secretB.y);
    System.out.println("M: " + M);
    System.out.println("h: " + h);
    System.out.println("Qm: " + Qm.x + ", " + Qm.y);
    System.out.println("Texto cifrado: " + cipherMessage.x + ", " + cipherMessage.y);
  }
  
  // Programa principal
  public static void main(String[] args) {
    Scanner keyboard = new Scanner(System.in);
    int p, a, b, x, y, dA, dB;
    Point g;
    
    System.out.print("\n");
    System.out.println("*****************************************************");
    System.out.println("* Seguridad en Sistemas Informáticos                *");
    System.out.println("* Práctica 10. Diffie-Hellman y ElGamal Elípticos   *");
    System.out.println("*****************************************************");
    System.out.print("\n");
    
    while(true) {
      System.out.println("Introduzca el primo p: ");
      p = keyboard.nextInt();
      if (!lehmann(p, 100)) {
        System.out.println("P no es primo");
        continue;
      }
      break;
    }
    System.out.println("");
    while(true) {
      System.out.println("Introduzca el valor de a: ");
      a = keyboard.nextInt();
      if(a < 0 || a >= p) {
        System.out.println("El valor de a debe estar en el rango [0, p-1]");
        continue;
      }
      System.out.println("Introduzca el valor de b: ");
      b = keyboard.nextInt();
      if(b < 0 || b >= p) {
        System.out.println("El valor de b debe estar en el rango [0, p-1]");
        continue;
      }
      if(mod(4*a*a*a + 27*b*b, p) == 0) {
        System.out.println("Los valores de a y b no cumplen con la fórmula 4a^3+27b^2(mod p) != 0");
        continue;
      }
      break;
    }
    System.out.println("");
    while(true) {
      System.out.println("Introduzca la coordenada X del punto G: ");
      x = keyboard.nextInt();
      System.out.println("Introduzca la coordenada Y del punto G: ");
      y = keyboard.nextInt();

      if (mod(y * y, p) != mod(x * x * x + a * x + b, p)) {
        System.out.println("Punto G no pertenece a la curva");
        continue;
      }
      break;
    }
    System.out.println("");
    while(true) {
      System.out.println("Introduzca la clave privada de Alice: ");
      dA = keyboard.nextInt();
      if (dA < 0 || dA >= p) {
        System.out.println("La clave privada de Alice no está en el rango [0, p-1]");
        continue;
      }
      break;
    }
    System.out.println("");
    while (true) {
      System.out.println("Introduzca la clave privada de Bob: ");
      dB = keyboard.nextInt();
      if (dB < 0 || dB >= p) {
        System.out.println("La clave privada de Bob no está en el rango [0, p-1]");
        continue;
      }
      break;
    }
    System.out.println("");
    System.out.println("Introduzca el mensaje m como entero: ");
    int m = keyboard.nextInt();
    System.out.println("");
    System.out.println("------------------");
    System.out.println("Procesando...");
    System.out.println("------------------\n");
    
    elipticGamal(p, a, b, new Point(x, y), dB, dA, m);
    
   // Graph graph = new Graph("Curva", a, b);
   // graph.pack();
   // graph.setVisible(true);
  }
}
