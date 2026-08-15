p = '<REPO>/src/main/java/com/trolmastercard/sexmod/client/gui/GirlInventorySlot.java'
s = open(p).read()
s = s.replace('GirlInventorySlot(GirlInventorySlot.b var1, IItemHandler var2, int var3, int var4, int var5) {\n      super(var2, var3, var4, var5);\n      this.slotType = var1;',
              'GirlInventorySlot(GirlInventorySlot.b slotType, IItemHandler itemHandler, int index, int xPosition, int yPosition) {\n      super(itemHandler, index, xPosition, yPosition);\n      this.slotType = slotType;')
s = s.replace('''   /**
    * Whether the stack fits the slot type for the given slot id.
    *
    * @param var1 the stack to test
    * @param var2 the girl slot id (0=weapon, 1=bow, 2=helmet, 3=chestplate,
    *             4=pants, 5=shoes, 6=rod)
    */
   public static boolean isSlotCompatible(ItemStack var0, int var1) {
      return isSlotTypeCompatible(var0, GirlInventorySlot.b.getSlotType(var1));
   }

   public boolean isItemValid(ItemStack var1) {
      return isSlotTypeCompatible(var1, this.slotType);
   }''',
'''   /**
    * Whether the stack fits the slot type for the given slot id.
    *
    * @param stack the stack to test
    * @param slotId the girl slot id (0=weapon, 1=bow, 2=helmet, 3=chestplate,
    *              4=pants, 5=shoes, 6=rod)
    */
   public static boolean isSlotCompatible(ItemStack stack, int slotId) {
      return isSlotTypeCompatible(stack, GirlInventorySlot.b.getSlotType(slotId));
   }

   public boolean isItemValid(ItemStack stack) {
      return isSlotTypeCompatible(stack, this.slotType);
   }''')
s = s.replace('static boolean isSlotTypeCompatible(ItemStack var0, GirlInventorySlot.b var1) {\n      Item var2 = var0.getItem();\n      switch (var1) {',
              'static boolean isSlotTypeCompatible(ItemStack stack, GirlInventorySlot.b slotType) {\n      Item item = stack.getItem();\n      switch (slotType) {')
s = s.replace('return var2 instanceof', 'return item instanceof')
s = s.replace('((ItemArmor)var2).armorType', '((ItemArmor)item).armorType')
s = s.replace('public static GirlInventorySlot.b getSlotType(int var0) {\n         switch (var0) {',
              'public static GirlInventorySlot.b getSlotType(int slotId) {\n         switch (slotId) {')
s = s.replace('throw new NullPointerException("Girls don\'t have a slot nr. " + var0);',
              'throw new NullPointerException("Girls don\'t have a slot nr. " + slotId);')
s = s.replace('b(int var3) {\n         this.id = var3;', 'b(int id) {\n         this.id = id;')
open(p, 'w').write(s)
print('done')
