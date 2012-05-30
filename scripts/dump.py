#!/usr/bin/python                                                                                                                                                                            

import sys

f = open(sys.argv[1], 'rb')
bytes = bytearray(f.read())[4:]

def hash(word):
  result = 0
  stop = len(word)
  for i in range(0, stop):
    result = result * 31
    result += ord(word[i])
    if (i == 3):
      return result
  return result

class Node:

  def __init__(self, data):
    self.b = data

  def __str__(self):
    result = "{\n"
    result += '    n: ' + str(self.n) + '\n'
    result += '    key: "' + self.key + '"\n'
    if self.value:
      result += '    value: ' + str(self.value) + '\n'
    if self.left:
      result += '    left: ' + str(self.left) + '\n'
    if self.child:
      result += '    child: ' + str(self.child) + '\n'
    if self.right:
      result += '    right: ' + str(self.right) + '\n'
    result += '    next: ' + str(self.next) + '\n'
    result += "}\n"
    return result

  def next_non_descendent(self):
    while self.child is not None:
      self.move(self.child)
      while self.right is not None:
        self.move(self.right)
    return self.next

  def move(self, n):
    self.n = n
    key_length = self.b[n]
    flags = self.b[n + 1]
    index = n + 2

    # value                                                                                                                                                                                  
    if flags & (1 << 0):
      self.value = sum((self.b[index + i] << ((3 - i) * 8)) for i in range(4))
      index += 4
    else:
      self.value = None

    if flags & (1 << 1):
      self.left = sum((self.b[index + i] << ((3 - i) * 8)) for i in range(4))
      index += 4
    else:
      self.left = None


    if flags & (1 << 2):
      self.right = sum((self.b[index + i] << ((3 - i) * 8)) for i in range(4))
      index += 4
    else:
      self.right = None

    if flags & (1 << 3):
      self.child = sum((self.b[index + i] << ((3 - i) * 8)) for i in range(4))
      index += 4
    else:
      self.child = None

    self.key = ""
    stop = index + key_length
    while index < stop:
      self.key += chr(self.b[index])
      index += 1
    self.next = index

node = Node(bytes)

stop = len(bytes)
n = 0

key = ""
tuples = [(stop, 0)]

#
#x = 0
#while x < stop:
#  node.move(x)
#  print node
#  x = node.next

node.move(0)  
while tuples:
  if n >= tuples[-1][0]:
    (_, chars) = tuples.pop()
    key = key[0:(0 - chars)]
  else:
    node.move(n)
    k = node.key
    v = node.value
    n = node.next
    nna = node.next_non_descendent()
    tuples.append((nna, len(k)))
    key += k
    if v is not None:
      print '{0}\t{1}'.format(key, v)
