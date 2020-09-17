# -*- coding: utf-8 -*-
"""
Created on Wed May 13 17:05:27 2020

@author: Ильдус Яруллин
"""


import os
from kivymd.app import MDApp
from kivy.uix.floatlayout import FloatLayout
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.image import Image
from kivy.core.window import Window
from kivy.lang import Builder
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.widget import Widget
from kivy.uix.image import Image
from kivy.uix.image import AsyncImage

from kivymd.uix.menu import MDDropdownMenu
from kivymd.theming import ThemableBehavior
from kivymd.uix.list import OneLineListItem, MDList
from kivy.properties import StringProperty
from kivy.properties import NumericProperty, ReferenceListProperty, ObjectProperty
from kivy.clock import Clock
import platform
import time

from kivy.storage.jsonstore import JsonStore
from kivy.graphics.texture import Texture
from kivy.uix.camera import Camera
from kivy.graphics.opengl import glReadPixels
from kivy.utils import platform

from jnius import autoclass
from jnius import JavaException
try:
	from android.permissions import request_permissions, Permission
except:
			print('Error_permissions_importpackage')
print('Hi')
#Window.size=[240,480]
Window.clearcolor = (0, 0.67, 1, 1) #00abff

Activity = autoclass('org.kivy.android.PythonActivity')
activity = Activity.mActivity

lang='en'

class ContentNavigationDrawer(BoxLayout):
    pass

class myCamera(Camera):
	pass

class NavLayout(Screen):
	pass


class ItemDrawer(OneLineListItem):
    icon = StringProperty()


class DrawerList(ThemableBehavior, MDList):
	def set_color_item(self, instance_item):
		"""Called when tap on a menu item."""

		# Set the color of the icon and text for the menu item.
		for item in self.children:
			if item.text_color == self.theme_cls.primary_color:
				item.text_color = self.theme_cls.text_color
				break
		instance_item.text_color = self.theme_cls.primary_color
		if instance_item.text==App.store.get(lang)['main0']:
			App.root.current="face"
		elif instance_item.text==App.store.get(lang)['main-1']:
			App.root.current="menu"

class PredictContainer(Screen):
	def on_enter(self):
		self.cam = myCamera(index=1,play=True, size_hint=(1,0.75),resolution = (1280, 960), pos_hint={'center_x':0.5,'center_y':0.5})
		self.add_widget(self.cam)
		if (App.store.exists(lang)==True):
			self.ident.text=App.store.get(lang)['k3']
		#Clock.schedule_once(self.update, 6.0)
	def on(self):
		self.cam.export_to_png(filename="predict.png")
		Clock.schedule_once(self.update, 2.0)

	def update(self,dt):
		if activity.Predict("predict.png")==True:
			App.root.Form2()
		else:
			# ident: 4
			if (App.store.exists(lang)==True):
				self.ident.text=App.store.get(lang)['k4']
			#self.ident.
	#def update(self,dt):
	#	Activity = autoclass('org.kivy.android.PythonActivity')
	#	activity = Activity.mActivity
	#	self.cam.export_to_png(filename="predict.png")
	#	if activity.Predict("predict.png")==True:
	#		App.root.Form2()
	#	else:
	#		Clock.schedule_once(self.update, 3.0)

class MainContainer(Screen):
	def StartStop(self,state):
		if state == 'down':
			App.store.put('use',uses='2',id_=App.store.get('use')['id_'])
			
			activity.restartNotify(self.drop.text)
		else:
			activity.stopNotify()
			App.store.put('use',uses='1',id_=App.store.get('use')['id_'])
	def Start(self):
		App.store.put('use',uses='0',id_='0')
	def on_enter(self):
		Clock.schedule_once(self.init_,2.0)
	def setting(self, instance):
		self.drop.set_item(instance.text)
		print('Hellooo')
		self.menu.dismiss()
	def init_(self,dt):
		menu_items = [{"text": "5сек."},{"text": "10сек."},{"text": "15сек."},{"text": "30сек."},{"text": "1мин."},{"text": "3мин."}]
		self.menu = MDDropdownMenu(
		caller=self.drop,
		items=menu_items,
		position="center",
		callback=self.setting,
		width_mult=4
		)
		#self.menu.bind(on_release=self.setting())
	

class TrainContainer(Screen):
	def train(self):
		print('HiGGG')
		if platform=="android":
			Hardware = autoclass('org.myapp.Hardware')
			hardware = Hardware()
			hardware.train()
			time.sleep(4)
		App.root.Form2()
	def on_enter(self):
		if (App.store.exists(lang)==True):
			self.ident2.text=App.store.get(lang)['k2']
class FImage(AsyncImage):
	pass
class FaceContainer(Screen):
	def on_enter(self):
		print("me")
		self.grid.clear_widgets()
		icons_item = {"-1": "Главная","0": "Лица","1": "Контакты"}
		if (App.store.exists(lang)==True):
			for it in icons_item:
				icons_item[it]=App.store.get(lang)['main'+it]
		print("me")
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.clear_widgets()
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["-1"]))
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["0"]))
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["1"]))
		images=activity.Images()
		print("me")
		for image in images:
			print(image)
			print("me")
			self.grid.add_widget(FImage(source=image,size_hint=[1,1]))
		if (App.store.exists(lang)==True):
			self.ident5.text=App.store.get(lang)['k5']
			self.ident6.text=App.store.get(lang)['k6']
	def first(self):
		App.root.current="first"
	def delete(self):
		if len(activity.Images())>1:
			activity.delete(self.grid.index+1)
		
bb=0
class firstContainer(Screen):
	i=1
	def on(self):
		timestr = time.strftime("%Y%m%d_%H%M%S")
		self.cam.export_to_png(filename=App.store.get('use')['id_']+'-selfie_'+str(self.i)+".png")
		self.i=self.i+1
		if self.i==6:
			App.root.current="train"
			App.store.put('use',uses='1',id_=App.store.get('use')['id_'])
	def enter(self):
		print('Hi1')
		self.i=1
		self.cam = myCamera(index=1,play=True, size_hint=(1,0.75),resolution = (1280, 960), pos_hint={'center_x':0.5,'center_y':0.5})
		print('Hi1')
		self.add_widget(self.cam)
		print('hi')
		g=len(activity.Images())+1
		if g<1:
			g=1
		App.store.put('use',id_=str(g),uses=App.store.get('use')['uses'])
		print(App.store.get("en")['k1'])
		print(App.store.get("ru")['k1'])
		print(App.store.get("zh")['k1'])
		print(lang)
		if (App.store.exists(lang)==True):
			print(lang)
			print(App.store.get(lang)['k1'])
			self.ident1.text=App.store.get(lang)['k1']

class StartContainer(Screen):
    pass

class Container(Screen):
	def __new__(secondContainer, *args, **kwargs):
		return super().__new__(secondContainer)
	
	def Start_(self):
		print('Hi')
		App.root.current='menu'
		icons_item = {"0": "Лица","1": "Контакты"}
		if (App.store.exists(lang)==True):
			for it in icons_item:
				icons_item[it]=App.store.get(lang)['main'+it]
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["0"]))
		App.root.current_screen.nav_layout.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["1"]))
        #lock.schedule_interval(App.root.current_screen.update, 1.0)

	def change_image(self,LR):
		if LR=='R':
			self.img_start.load_next()

			if self.img_start.index+1==2:
				self.arrow_rigth.size_hint=0,0.05
			else:
				self.arrow_left.size_hint=0.05,0.05
				self.arrow_rigth.size_hint=0.05,0.05
		elif LR=='L':
			self.img_start.load_previous()
			if self.img_start.index-1==0:
				self.arrow_left.size_hint=0,0.05
			else:
				self.arrow_left.size_hint=0.05,0.05
				self.arrow_rigth.size_hint=0.05,0.05

class sm(ScreenManager):
	def Enter_(self):
		print('Hi')
		#self.add_widget(facesContainer(name="faces"))
		if (App.store.exists('use')==False):
			App.store.put('use',uses='0',id_='0')
			print('Hi')
		if (App.store.get('use')['uses']=='0'):
			print('Hi')
			self.current='first'
			print('Hi')
		elif (App.store.get('use')['uses']=='2') or (App.store.get('use')['uses']=='1'):
			k=0
			self.current="predict"
		print(self.current)
	
	def Form2(self):
		self.current='menu'
		icons_item = {"0": "Лица","1": "Контакты"}
		if (App.store.exists(lang)==True):
			for it in icons_item:
				icons_item[it]=App.store.get(lang)['main'+it]
		print(icons_item["0"])
		self.current_screen.nav_layout.ids.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["0"]))
		print(icons_item["1"])
		self.current_screen.nav_layout.ids.content_drawer.ids.md_list.add_widget(ItemDrawer(text=icons_item["1"]))
		if (App.store.exists('use')==True):
			if (App.store.get('use')['uses']=='2'):
				self.current_screen.startBut.state="down"
		
Labl="""
<Label>:
	font_name: "DF.ttf"
"""
        

class MyApp(MDApp):
	store=JsonStore('Parametr.json')
	def build(self):
		if lang=="zh":
			Builder.load_string(Labl)
		print(Labl)
		#self.icon_folder=
		try:
			request_permissions([Permission.CAMERA,Permission.WRITE_EXTERNAL_STORAGE,Permission.READ_EXTERNAL_STORAGE])
		except:
			print('Error_permissions')
		
		Sm=sm()
		print('Hi')
		Sm.on_pre_enter=Sm.Enter_()
		return Sm
	def first(self):
		App.root.current="first"

if __name__ == '__main__':
	App=MyApp()
	lang=activity.getLang()
	
	print(lang)
	print(activity.getLang())
	if (App.store.exists("en")==True):
		print("en exists")
	if (App.store.exists("ru")==True):
		print("ru exists")	
	if (App.store.exists("zh")==True):
		print("zh exists")
	App.run()
	#cv2.destroyAllWindows()