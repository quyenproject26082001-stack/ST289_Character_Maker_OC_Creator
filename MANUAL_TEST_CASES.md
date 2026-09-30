# Manual test cases — ST258

Checklist để tự test app Android. Đây là kết quả mong đợi để đánh giá Pass/Fail, không phải xác nhận rằng các case đã chạy thành công.

## Chuẩn bị

- Ghi phiên bản APK, ngày build, thiết bị, Android, độ phân giải và ngôn ngữ.
- Chạy trên ít nhất một điện thoại nhỏ, một điện thoại lớn; chạy lại các case UI trên tablet nếu có. Chỉ test xoay màn hình nếu app cho phép xoay.
- Dùng ảnh/dữ liệu test cho các case xóa. Chuẩn bị ít nhất 10 Avatar thật và 10 Design thật để danh sách cuộn nhiều hàng; có một nhân vật hỗ trợ Edit.
- Chuẩn bị một trạng thái dữ liệu rỗng và một trạng thái chỉ có 1–2 item, trên thiết bị/profile test riêng nếu cần.
- Chuẩn bị mạng bật/tắt; WhatsApp và Telegram để kiểm tra tích hợp.
- Hiện `MyCreationPreviewConfig.SHOW_FAKE_ITEMS=false`. Các case chức năng dùng dữ liệu thật. Các case PV cần APK riêng với preview bật; tài liệu này không đổi config.
- `P0`: luồng chính cần chạy trước. `P1`: hồi quy và UI.
- Điền cột Kết quả: `Pass`, `Fail`, `Blocked` hoặc `N/A`. Với Fail, ghi bước lỗi và đính kèm ảnh/video. Với Blocked, ghi điều kiện còn thiếu.
- Nếu có quảng cáo ở bước chuyển màn, đóng quảng cáo rồi kiểm tra app tiếp tục đúng luồng. Không coi việc không có quảng cáo là lỗi khi không có quảng cáo được tải.

## 1. Mở app và điều hướng

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| NAV-01 | P0 | Mở app từ trạng thái đã đóng; chờ Splash và hoàn thành màn đầu vào nếu xuất hiện. | Đến Home; không kẹt Splash/loading hoặc màn trắng. | |
| NAV-02 | P1 | Đổi ngôn ngữ; quay về Home; mở Settings và một dialog xác nhận. | Ngôn ngữ đã chọn được áp dụng; chữ có dấu hiển thị đúng, không xuất hiện tên resource hoặc chữ bị cắt mất nội dung. | |
| NAV-03 | P1 | Ở màn xin quyền, từ chối quyền; thử tiếp tục. Nếu app yêu cầu lại, cấp quyền ở lần tiếp theo. | App xử lý cả từ chối và cấp quyền; không crash, không tự coi quyền đã được cấp khi người dùng từ chối. | |
| NAV-04 | P0 | Từ Home lần lượt mở Character Maker, Challenge Character, Character Random và My Creation; quay lại sau mỗi lần. | Mỗi nút mở đúng chức năng; Back quay lại hợp lý, không mở nhiều bản sao màn hình. | |
| NAV-05 | P1 | Tắt mạng trước khi mở một chức năng cần dữ liệu online; sau đó bật mạng và thử lại. | Có thông báo/lối retry phù hợp; sau khi mạng trở lại có thể sử dụng chức năng, không kẹt loading hoặc nút bị khóa mãi. | |

## 2. Customize: layer và long press

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| CUS-01 | P0 | Chọn một nhân vật; thay lần lượt ít nhất 3 layer và đổi màu một layer hỗ trợ màu. | Preview cập nhật đúng lựa chọn; item được chọn khớp với hình hiển thị. | |
| CUS-02 | P1 | Chọn layer A; mở Move; đóng Move; chọn layer B; mở lại Move. | Panel mở/đóng đúng; controls tác động tới layer đang chọn. | |
| CUS-03 | P0 | Ở Move, chạm nhanh từng nút trái/phải/lên/xuống một lần. | Layer dịch một bước nhỏ đúng hướng; không tự chạy tiếp sau khi thả. | |
| CUS-04 | P0 | Giữ một nút hướng khoảng 1–2 giây; quan sát tốc độ trước khi layer chạm giới hạn. Lặp lại 4 hướng. | Sau khoảng 400 ms bắt đầu lặp; tốc độ tăng dần từ 1x lên tối đa 4x. | |
| CUS-05 | P0 | Giữ nút hướng rồi thả; chờ 1 giây; nhấn giữ lại hoặc đổi hướng. | Layer dừng ngay khi thả; lần giữ mới bắt đầu lại ở tốc độ thấp, không kế thừa gia tốc cũ. | |
| CUS-06 | P1 | Đang giữ nút hướng, đưa app ra nền bằng Home hệ thống; quay lại. | Không còn chuyển động ngoài ý muốn, không có callback giữ nút chạy mãi. | |
| CUS-07 | P0 | Giữ di chuyển tới giới hạn; thử tiếp tục theo hướng đó, rồi nhấn hướng ngược lại. | Không vượt giới hạn cho phép; nút ở giới hạn bị vô hiệu hóa/mờ; hướng ngược lại vẫn hoạt động. | |
| CUS-08 | P1 | Thay đổi Move, rotate, scale trên layer A; chuyển sang layer B và thao tác; trở lại A. | Transform của từng layer độc lập; quay lại A vẫn hiển thị đúng transform của A. | |
| CUS-09 | P1 | Nhấn/giữ rotate và scale; kiểm tra cả hai chiều; thử scale tới giới hạn. | Rotate/scale hoạt động; không bị thêm gia tốc của nút hướng; scale nằm trong giới hạn 0.5x–1.5x. | |
| CUS-10 | P0 | Thay đổi vị trí, góc và scale; nhấn Reset Move. | Layer đang chọn về vị trí/góc/scale ban đầu; trạng thái nút reset cập nhật đúng. | |

## 3. Text, bảng màu và bóng trắng

Kích thước `36/12/16dp` dùng dimen của app: trên điện thoại là 36/12/16dp; tablet hiện có hệ số scale riêng. Không đánh Fail trên tablet chỉ vì giá trị lớn hơn điện thoại. Kích thước dp không bằng số pixel trong screenshot trừ khi density là 1.

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| UI-01 | P1 | Mở Add Character → Text; quan sát hàng Font và Color, trước và sau khi cuộn ngang. | Item font và màu chữ có khung 36×36dp trên điện thoại; chọn/bỏ chọn không làm thay đổi kích thước. | |
| UI-02 | P1 | Đưa cả hai hàng về đầu danh sách; kiểm tra khoảng cách bằng Layout Inspector hoặc ảnh có quy đổi density. | Item đầu cách cạnh bắt đầu danh sách 16dp; khoảng hở giữa hai item liền kề là 12dp, không cộng thêm margin làm thành 24dp. | |
| UI-03 | P0 | Nhập chữ có dấu; đổi font, màu, thêm màu tùy chọn; nhấn Done. | Nội dung chữ, font và màu cập nhật đúng trên canvas; không mất nội dung sau khi đổi lựa chọn. | |
| UI-04 | P1 | Mở Text, focus ô nhập; mở bàn phím; nhấn Back hệ thống một lần, sau đó nhấn Back lần nữa. | Lần đầu xử lý bàn phím/focus; khi thoát editor phải có xác nhận, không mất nội dung do vô tình nhấn Back. | |
| UI-05 | P1 | Ở Customize, mở bảng màu trên layer có hỗ trợ màu; chụp ảnh và so với Figma. | Nền gradient và viền giữ nguyên; có bóng trắng mềm 50%, lệch xuống 2dp, blur mục tiêu 2dp; không thành một dải trắng sắc cạnh. | |
| UI-06 | P1 | Lặp lại UI-05 ở CosplayCustomize; đổi layer để chiều rộng bảng màu thay đổi; đóng/mở lại. | Bóng đi theo đúng chiều rộng bảng; không bị cắt ở cạnh/dưới, không lưu vệt bóng cũ; phần nền chính vẫn cao 40dp theo dimen hiện tại. | |
| UI-07 | P1 | Trong Add Character thêm lần lượt background, sticker, speech và text; chọn, di chuyển, scale/rotate rồi lưu. | Thành phần mới xuất hiện, thao tác đúng đối tượng; ảnh kết quả chứa các thành phần đã thêm. | |

## 4. Confirm Exit và Cosplay

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| EXIT-01 | P0 | Ở Customize có thay đổi chưa lưu, nhấn Back trên action bar; chọn No. | Dialog xác nhận xuất hiện; No đóng dialog, vẫn ở màn Customize và giữ thay đổi. | |
| EXIT-02 | P0 | Ở CosplayCustomize có thay đổi, nhấn Back trên action bar; chọn No. | Có dialog xác nhận; vẫn ở màn Cosplay và giữ nhân vật đang chỉnh. | |
| EXIT-03 | P0 | Lặp lại EXIT-01 và EXIT-02 bằng Back hệ thống hoặc gesture Back; chọn No. | Cả hai đường Back đều có xác nhận; không thoát trực tiếp. | |
| EXIT-04 | P0 | Ở từng màn Customize/CosplayCustomize, mở dialog rồi chọn Yes; đóng quảng cáo nếu xuất hiện. | Chỉ thoát sau khi xác nhận; về màn trước đúng một lần, không kẹt dialog/loading. | |
| EXIT-05 | P1 | Khi dialog Exit xuất hiện, chạm ngoài dialog và nhấn Back hệ thống; sau đó chọn No. | Với dialog hiện tại, chạm ngoài/Back không tự đóng; nút No vẫn đóng được, UI toàn màn hình được phục hồi. | |
| COS-01 | P0 | Mở một phiên Challenge/Cosplay; xem ảnh mẫu; chỉnh layer đúng/sai rồi chuyển tới kết quả. | Ảnh mẫu hiển thị được; tiến độ/điểm phản ánh thao tác; kết quả không bị mở lặp nhiều lần. | |
| COS-02 | P1 | Quan sát progress ở 0%, mức giữa và gần/đạt 100%. | Phần fill và thumb nằm trong thanh; hình progress không bị kéo méo; con số khớp mức tiến độ. | |
| COS-03 | P1 | Khi countdown chạy, đưa app ra nền và quay lại; tiếp tục tới hết thời gian một phiên test. | Không có nhiều timer chạy đồng thời; trạng thái khi quay lại hợp lý; hết thời gian chuyển sang kết quả một lần. | |
| COS-04 | P0 | Ở màn kết quả nhấn Try Again, sau đó thử nút Home. | Try Again mở được phiên mới; Home về Home; không hiển thị lại dialog kết quả của phiên cũ. | |

## 5. My Creation: normal/select ở cả hai tab

Chạy MC-01 đến MC-08 với danh sách dài, rồi lặp MC-01/02/03/05 với 1–2 item. Riêng các case xóa chỉ dùng dữ liệu test.

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| MC-01 | P0 | Mở My Character ở normal mode; cuộn giữa danh sách và xuống cuối. | WhatsApp/Telegram hiển thị; item không vẽ phía sau nút; có khoảng hở 12dp theo dimen; hàng cuối cuộn lên xem đầy đủ được. | |
| MC-02 | P0 | Mở My Design ở normal mode; cuộn giữa và cuối danh sách. | Không xuất hiện cụm nút normal dành cho Avatar; hàng cuối xem đầy đủ được, không bị chừa khoảng trống lớn do margin của tab trước. | |
| MC-03 | P0 | Ở My Character, long press một item; cuộn danh sách xuống cuối. | Vào select mode; item long press được chọn; WhatsApp/Telegram và Share/Download hiển thị; item không che nút, hàng cuối cuộn lên đủ. | |
| MC-04 | P0 | Ở My Design, long press một item; cuộn xuống cuối. | Vào select mode; Share/Download hiển thị; không hiện WhatsApp/Telegram của Avatar; không che hàng cuối. | |
| MC-05 | P0 | Ở mỗi tab, vào rồi thoát select mode 5 lần, có cuộn giữa các lần. | Bottom, checkbox và khoảng chừa khôi phục đúng mode; khoảng trắng không cộng dồn, không nhấp nháy/lặp layout liên tục. | |
| MC-06 | P1 | Chọn một số item ở tab A; chuyển sang tab B rồi quay lại A. | Trạng thái tab và controls nhất quán; không mang lựa chọn của tab A sang B hoặc giữ nút select sai mode. Ghi rõ nếu app giữ lựa chọn khác mong đợi. | |
| MC-07 | P0 | Vào select mode; tick/untick từng item; nhấn Select All, rồi bỏ chọn tất cả. | Checkbox, lớp phủ và icon Select All khớp dữ liệu; không tick nhầm item do RecyclerView tái sử dụng view. | |
| MC-08 | P1 | Mở một item thật để xem, sau đó Back về My Creation. | Đúng tab được hiển thị; mode trở về normal theo luồng hiện tại; không còn khoảng chừa của select mode hoặc checkbox sót lại. | |
| MC-09 | P0 | Select mode: chọn 2 ảnh thật rồi Share; hủy share sheet và thử Download. | Share sheet nhận đúng ảnh đã chọn; hủy quay về app; Download lưu đúng ảnh và phản hồi kết quả. | |
| MC-10 | P1 | Select mode: bỏ chọn tất cả; thử Share và Download. | Không share/lưu ảnh ngoài lựa chọn, không crash; có phản hồi yêu cầu chọn ảnh khi thao tác cần ảnh. | |
| MC-11 | P0 | Chọn một item test; nhấn Delete; chọn No. Lặp lại rồi chọn Yes; mở lại My Creation. | No giữ item; Yes xóa đúng item; mở lại không thấy item đã xóa, các item khác vẫn còn. | |
| MC-12 | P1 | Ở Avatar normal, nhấn Add To WhatsApp/Telegram; sau đó vào select, chọn một nhóm ảnh và thử lại. | Normal dùng danh sách thật của tab; select dùng nhóm đã chọn; không lấy nhầm ảnh từ Design. Thiếu app hoặc số lượng sticker không hợp lệ có thông báo phù hợp. | |
| MC-13 | P0 | Với dữ liệu rỗng, mở từng tab; hoặc xóa item cuối trên thiết bị test. | Hiển thị trạng thái không có item; bottom không còn hiển thị cho danh sách rỗng, select mode được thoát hợp lý. | |
| MC-14 | P0 | Mở Edit trên Avatar thật còn dữ liệu layer; đổi một layer và lưu; xem lại kết quả. | Mở được dữ liệu chỉnh sửa đúng nhân vật; thay đổi đã lưu xuất hiện đúng khi xem lại; không ảnh hưởng item khác. | |

## 6. Lưu và hồi quy sau khi dọn string

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| REG-01 | P0 | Tạo nhân vật ở Customize; Next sang bước thêm nội dung; lưu tới màn Success. | Ảnh kết quả đúng layer/màu/transform và nội dung thêm; loading được đóng, không kẹt màn hình. | |
| REG-02 | P0 | Sau khi lưu, mở My Creation; kiểm tra dữ liệu tương ứng; đóng và mở lại app. | Dữ liệu thật đã lưu được đọc lại, không chỉ tồn tại trong phiên hiện tại; ảnh không trống/hỏng. | |
| REG-03 | P1 | Chạm nhanh nhiều lần vào Next/Save/Download trong khi thao tác đang xử lý. | Không crash, không mở chồng nhiều màn hoặc tạo nhiều tác vụ/lưu trùng ngoài ý muốn. | |
| REG-04 | P1 | Ở Success thử Download, Share và quay về Home/My Creation. Với app nhận share chưa cài, thử lại tương ứng. | Các đường điều hướng hoạt động; hủy share quay lại an toàn; app nhận chưa cài được xử lý, không crash. | |
| REG-05 | P1 | Chạy các màn/dialog chính bằng EN và ít nhất một ngôn ngữ có chữ dài như DE/FR; kiểm tra thêm ngôn ngữ bạn muốn phát hành. | String còn dùng hiển thị đầy đủ hoặc marquee/autosize phù hợp; không mất chữ sau cleanup, không hiện mã resource, không lỗi font/dấu. | |
| REG-06 | P1 | Lặp MC-01 đến MC-05 và UI-01/02/05/06 trên màn hình lớn hoặc tablet. | Khoảng chừa list đi theo kích thước bottom thật; hình/nút không chồng nhau; bóng và bảng màu không bị cắt. | |

## 7. Preview — chỉ chạy trên APK bật preview

Preview hiện vẫn là item giả, chưa được chuyển thành dữ liệu thật. Không dùng PV để xác nhận tính năng lưu/xóa/share dữ liệu thật.

| ID | Mức | Tiền điều kiện / bước thực hiện | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| PV-01 | P1 | APK có `SHOW_FAKE_ITEMS=true`, `FAKE_ITEM_COUNT=20`; mở cả hai tab My Creation. | Mỗi tab hiển thị 20 item mẫu; trạng thái có item được nhận diện đúng dù không có đường dẫn file thật. | |
| PV-02 | P1 | Trên APK preview, chạy lại MC-01 đến MC-05. | Normal/select có bottom đúng tab; margin/padding động hoạt động giống danh sách thật; item không che nút. | |
| PV-03 | P1 | Chọn item giả; thử mở/Edit/Delete/Share/Download; sau đó dùng APK preview tắt trên cùng dữ liệu app. | Item giả không được gửi vào thao tác file thật, không crash; khi preview tắt, item giả biến mất và dữ liệu thật đã có vẫn đọc được. | |

## Ghi nhận lỗi

```text
Test case ID:
APK / ngày build:
Thiết bị / Android / ngôn ngữ:
Preview true hay false:
Tab / normal hay select:
Tiền điều kiện và dữ liệu đã chọn:
Các bước tái hiện:
Kết quả mong đợi:
Kết quả thực tế:
Tần suất lỗi: ... / ... lần
Ảnh hoặc video:
```

Vòng test nhanh nên chạy P0 trước. Với thay đổi layout My Creation, tối thiểu chạy MC-01/02/03/04/05 trên điện thoại nhỏ và lớn, có cuộn đến cuối danh sách. Với bóng trắng, cần đối chiếu screenshot trực tiếp với Figma; build thành công chưa xác nhận bóng giống thiết kế.
