MVP
Программа для визуализации панорамных фотографий сделанных камерой Huawei CV60
<h3> Программа создавалась для личного пользования и не предусматривалось выкладывание в виде конечного продукта</h3>
<br/>Для визуализации использовалась JS бмблиотека Pannellum2_5_6
<br/>В библиотеку были внесены изменения, позволяющие  перемещатся по точкам без ограничений
<br/>Программа отслеживает появление  новых фотографий в каталоге /DCIM/CV60/ и создает JSON файл в котором описывает позиционирование камеры (азимут+наклон+GPS)
<br/>Если запускать внешнюю программу "com.huawei.cvIntl60", то запускается сервис, который автоматически привязывает  устройство к мировым координатам
<br/>Если после запуска программы обнаруживается в каталоге новые /DCIM/CV60/ фото, без JSON описания, тогда в этом каталгге автоматически создвется файл описания с  пустыми значениями  
<img src="https://github.com/MyasnikovIA/Huawei_Meta/blob/main/img/photo_2025-03-11_11-44-52.jpg?raw=true"/>
<br/>
<img src="https://github.com/MyasnikovIA/Huawei_Meta/blob/main/img/photo_2025-03-11_11-47-59.jpg?raw=true"/>