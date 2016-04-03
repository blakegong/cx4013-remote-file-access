"""
Implementation of client-side caching in memory.
"""

from datetime import datetime

from ClientProtocol import ProtocolLayer

class CacheEntry():
    """
    An entry of a cached file.
    """
    def __init__(self, pathname, ttl=10):
        self.pathname = pathname
        self.ttl = ttl
        self.content = dict()
        self.last_update = datetime.now()

    def get(self, offset, length):
        """
        Get content of cache entry. Throw exceptions if necessary.
        """
        if (datetime.now() - self.last_update).total_seconds() > self.ttl:
            raise Exception('Cache outdated. ')
        return b"".join(self.content[i] for i in range(offset, offset + length))

    def set(self, offset, content):
        """
        Set content of cache entry.
        """
        for i in range(len(content)):
            self.content[i + offset] = content[i:i+1]
        self.last_update = datetime.now()


class CacheLayer():
    """
    A cache layer for remote file access.
    """
    def __init__(self):
        self.entries = dict()
        self.PROTOCOL = ProtocolLayer()

    def READ(self, pathname, offset, length):
        """
        Caching for READ protocol.
        """
        try:
            return self.entries[pathname].get(offset, length)
        except:
            # Cache miss
            # -> read from server instead
            # -> set cache later
            print('Protocol READ not implemented yet.')
            pass


    def INSERT(self, pathname, offset, content):
        """
        Caching for INSERT protocol.
        """
        if offset < 0:
            return
        elif pathname in self.entries:
            # The entry already exists
            self.entries[pathname].set(offset, content)
        else:
            # The entry not yet exists
            new_entry = CacheEntry(pathname)
            new_entry.set(offset, content)
            self.entries[pathname] = new_entry

    def CLEAR(self, pathname):
        """
        Caching for CLEAR protocol.
        """
        self.PROTOCOL.CLEAR(pathname)
        try:
            del self.entries[pathname]
        except:
            pass

    def DELETE(self, pathname, offset, content):
        """
        Caching for DELETE protocol.
        """
        self.PROTOCOL.DELETE(pathname, offset, content)
        try:
            del self.entries[pathname]
        except:
            pass


if __name__ == '__main__':
    layer = CacheLayer()
    layer.INSERT('fileA', 0, b'abc')
    print(layer.READ('fileA', 1, 2))
