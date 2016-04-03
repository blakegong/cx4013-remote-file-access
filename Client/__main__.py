import cmd

from ClientCaching import CachedLayer

class ClientShell(cmd.Cmd):
    intro = '\nWelcome. \nType help or ? to list commands.\n'
    prompt = '>>> '
    file = None

    def do_READ(self, arg):
        """
        Read file from the server with *pathname*, *offset*, and *length*.
        EXAMPLE: READ example.txt 3 10
        """
        args = arg.split()
        cl.READ(args[0], int(args[1]), int(args[2]))

    def do_INSERT(self, arg):
        """
        Insert content into file on the server with *pathname*, *offset* and *content*.
        EXAMPLE: INSERT example.txt 0 Hello, world!
        """
        args = arg.split()
        cl.INSERT(args[0], int(args[1]), str.encode(' '.join(args[2:])))

    def do_MONITOR(self, arg):
        """
        Monitor a file on the server with *pathname*, and *duration* in seconds.
        EXAMPLE: MONITOR example.txt 30
        """
        args = arg.split()
        cl.MONITOR(args[0], int(args[1]))

    def do_CLEAR(self, arg):
        """
        Clear content of file on the server with *pathname*.
        EXAMPLE: CLEAR example.txt
        """
        cl.CLEAR(arg)

    def do_DELETE(self, arg):
        """
        Delete content from the file on the server with *pathname*, *offset*, *length*.
        EXAMPLE: DELETE example.txt 4 20
        """
        args = arg.split()
        cl.DELETE(args[0], int(args[1]), int(args[2]))

    def do_exit(self, arg):
        """
        Just exit.
        """
        return True

if __name__ == '__main__':
    cl = CachedLayer(ttl=20)
    ClientShell().cmdloop()
