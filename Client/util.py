import struct

def marshall(hashmap):
    #pack number of variables
    msg = bytearray(int2byte(len(hashmap)))
    #remaing variables
    for key in hashmap:
        msg += key + '\0:'
        #string
        if type(hashmap[key]) == str:
            msg += 'r' + hashmap[key] + '\0'
        #integer
        elif type(hashmap[key]) == int:
            msg += 'i'+int2byte(hashmap[key])
        #exception
        else:
            print '[Exception] type not known'
    return msg

def unmarshall(msg):
    #pack number of variables
    var_no = byte2int(msg[:4])
    response = {}
    index = 4
    #remaing variables
    while len(response) < var_no:
        #key
        end = index
        while msg[end] != '\0':
            end += 1
        key = msg[index:end]
        index = end+1
        # :
        if msg[index]!=':':
            print '[Exception] incoming package corrupted'
        index += 1
        #remaining
        data_type = msg[index]
        index += 1
        #string
        if data_type == 'r':
            end = index
            while msg[end] != '\0':
                end += 1
            value = msg[index:end]
            index = end+1
        #integer
        elif data_type == 'i':
            value = byte2int(msg[index:index+4])
            index += 4
        #exception
        else:
            print '[Exception] type not known'
        response[key] = value
    return response

def int2byte(num):
    return bytearray(struct.unpack("4B", struct.pack(">i", num)))

def byte2int(b):
    b = map(lambda x: ord(x), b)
    return (b[0] & 0xff) << 24 | (b[1] & 0xff) << 16| (b[2] & 0xff) << 8 | (b[3] & 0xff);
