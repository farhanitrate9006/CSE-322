package main;

import thread.ClientThread;
import java.util.Scanner;

public class Client
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        String command;

        while(true)
        {
            System.out.println("### Write <file-name> (Or <exit> to Exit) ###");
            command = sc.nextLine();
            if(command.equals("exit"))
                break;
            new ClientThread(command);
        }
    }
}
