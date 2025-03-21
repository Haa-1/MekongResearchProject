package com.example.researchproject.Profile;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;

public class GuideDetailActivity extends AppCompatActivity {

    private TextView tvGuideTitle, tvGuideContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);

        // Ánh xạ TextView
        tvGuideTitle = findViewById(R.id.tv_guide_title);
        tvGuideContent = findViewById(R.id.tv_guide_content);

        // Nhận dữ liệu từ Intent
        String title = getIntent().getStringExtra("GUIDE_TITLE");

        // Thiết lập tiêu đề
        tvGuideTitle.setText(title);

        // Thiết lập nội dung hướng dẫn (có thể cập nhật nội dung thực tế)
        String content = getGuideContent(title);
        tvGuideContent.setText(content);
    }

    // Hàm trả về nội dung hướng dẫn theo tiêu đề
    private String getGuideContent(String title) {
        switch (title) {
            case "Hướng dẫn chung":
                return "Ứng dụng MekongGo giúp kết nối chủ xe và khách thuê xe một cách nhanh chóng, tiện lợi và an toàn. \n\n" +
                        "Quy trình cho thuê xe và thuê xe:\n\n" +
                        "Đối với chủ xe:\n" +
                        "1. Đăng ký thành viên:\n" +
                        "   - Đăng ký tài khoản bằng số điện thoại hoặc email, Google.\n" +
                        "   - Xác minh danh tính bằng cách cung cấp thông tin cá nhân và giấy tờ cần thiết.\n\n" +
                        "2. Đăng ký xe cho thuê:\n" +
                        "   - Cung cấp thông tin chi tiết về xe: thương hiệu, đời xe, biển số xe, số km đã đi, hình ảnh xe, tình trạng, dịch vụ.\n" +
                        "   - Thiết lập giá thuê xe.\n" +
                        "   - Đặt lịch xe sẵn sàng cho thuê.\n\n" +
                        "3. Chờ Admin duyệt:\n" +
                        "   - Hệ thống sẽ kiểm tra và xác minh xe.\n" +
                        "   - Xe có thể được đưa lên hệ thống sau khi được duyệt.\n\n" +
                        "4. Nhận yêu cầu thuê xe:\n" +
                        "   - Khi có khách đặt xe, chủ xe sẽ nhận được thông báo.\n" +
                        "   - Xem xét thông tin thuê và quyết định chấp nhận hoặc từ chối.\n\n" +
                        "5. Bàn giao xe:\n" +
                        "   - Đưa xe đến khu vực đã thỏa thuận giao xe.\n" +
                        "   - Kiểm tra tình trạng xe.\n" +
                        "   - Chụp ảnh xe làm bằng chứng bàn giao trước khi cho thuê.\n\n" +
                        "6. Kết thúc giao dịch:\n" +
                        "   - Nhận lại xe sau khi khách trả xe đúng thời gian.\n" +
                        "   - Kiểm tra tình trạng xe, cập nhật lên hệ thống.\n" +
                        "   - Hoàn tất thủ tục trên ứng dụng.\n\n" +
                        "Đối với khách thuê:\n" +
                        "1. Đăng ký thành viên:\n" +
                        "   - Tạo tài khoản trên ứng dụng MekongGo.\n" +
                        "   - Xác minh danh tính bằng cách cung cấp thông tin cá nhân và giấy phép lái xe.\n\n" +
                        "2. Tìm xe:\n" +
                        "   - Sử dụng công cụ tìm kiếm để lọc xe theo địa điểm, loại xe, giá thuê, và thời gian mong muốn.\n\n" +
                        "3. Đặt xe:\n" +
                        "   - Chọn xe phù hợp và gửi yêu cầu thuê.\n" +
                        "   - Đợi chủ xe xác nhận.\n\n" +
                        "4. Thanh toán:\n" +
                        "   - Chọn phương thức thanh toán (thẻ tín dụng, ZaloPay, chuyển khoản, v.v.).\n" +
                        "   - Hoàn tất thanh toán để xác nhận đặt xe.\n\n" +
                        "5. Nhận xe:\n" +
                        "   - Đến địa điểm nhận xe đúng giờ.\n" +
                        "   - Kiểm tra tình trạng xe trước khi nhận.\n" +
                        "   - Ký hợp đồng thuê xe (nếu có yêu cầu từ chủ xe).\n\n" +
                        "6. Trả xe:\n" +
                        "   - Đưa xe về địa điểm đã thỏa thuận.\n" +
                        "   - Kiểm tra xe cùng chủ xe.\n" +
                        "   - Hoàn tất thủ tục trả xe trên ứng dụng.";

            case "Hướng dẫn đặt xe":
                return "2. HƯỚNG DẪN ĐẶT XE\n\n" +
                        "1. Đăng ký/ Đăng nhập tài khoản:\n" +
                        "   - Nhập số điện thoại hoặc email để đăng ký tài khoản.\n" +
                        "   - Xác minh tài khoản qua mã OTP gửi về điện thoại/email.\n\n" +
                        "2. Tìm kiếm xe:\n" +
                        "   - Nhập địa điểm nhận xe, ngày giờ thuê và trả xe.\n" +
                        "   - Lọc kết quả theo loại xe, giá thuê, hoặc khoảng cách.\n\n" +
                        "3. Lựa chọn xe mong muốn và gửi yêu cầu thuê xe:\n" +
                        "   - Xem thông tin chi tiết về xe: hình ảnh, giá thuê, chính sách của chủ xe.\n" +
                        "   - Thanh toán đầy đủ số tiền để xác nhận đặt xe.";

            case "Hướng dẫn thanh toán":
                return "3. HƯỚNG DẪN THANH TOÁN\n\n" +
                        "1. Điền đầy đủ thông tin:\n" +
                        "   - Cung cấp thông tin thanh toán chính xác.\n" +
                        "   - Xác nhận số tiền thanh toán và phương thức thanh toán.\n\n" +
                        "2. Bấm thanh toán qua ZaloPay:\n" +
                        "   - Chọn \"Thanh toán qua ZaloPay\".\n" +
                        "   - Hệ thống tự động chuyển sang ứng dụng ZaloPay để hoàn tất giao dịch.\n" +
                        "   - Sau khi thanh toán thành công, đơn hàng sẽ được xác nhận.";

            case "Quy chế hoạt động":
                return "1. Nguyên tắc chung:\n" +
                        "   MekongGo là nền tảng trung gian giúp kết nối chủ xe và khách thuê xe một cách thuận tiện, an toàn và minh bạch.\n" +
                        "   Tất cả các giao dịch thực hiện trên nền tảng phải tuân thủ pháp luật hiện hành và các quy định do MekongGo ban hành.\n\n" +

                        "2. Quy định chung:\n" +
                        "   - Người dùng khi đăng ký tài khoản trên MekongGo cần cung cấp thông tin cá nhân chính xác và trung thực.\n" +
                        "   - Chủ xe phải đảm bảo rằng phương tiện đăng ký cho thuê có đầy đủ giấy tờ hợp lệ, không thuộc diện tranh chấp pháp lý\n" +
                        "     và đáp ứng các tiêu chuẩn an toàn theo quy định.\n\n" +

                        "3. Quy trình giao dịch:\n" +
                        "   - Khách thuê tìm kiếm xe phù hợp và gửi yêu cầu thuê xe.\n" +
                        "   - Chủ xe xác nhận yêu cầu, thỏa thuận điều kiện thuê xe.\n" +
                        "   - Khách thuê thực hiện thanh toán qua hệ thống.\n" +
                        "   - Hai bên tiến hành giao nhận xe theo thời gian và địa điểm đã thống nhất.\n\n" +

                        "4. Đảm bảo an toàn giao dịch:\n" +
                        "   - MekongGo áp dụng các biện pháp bảo mật tiên tiến để bảo vệ thông tin người dùng cũng như đảm bảo an toàn\n" +
                        "     cho các giao dịch tài chính.\n" +
                        "   - Mọi giao dịch thanh toán được thực hiện qua các cổng thanh toán đáng tin cậy nhằm hạn chế rủi ro gian lận.\n\n" +

                        "5. Đảm bảo thông tin cá nhân khách thuê xe:\n" +
                        "   - Thông tin cá nhân của khách thuê xe được bảo vệ tuyệt đối và chỉ được sử dụng cho mục đích giao dịch trên nền tảng MekongGo.\n" +
                        "   - Chúng tôi cam kết không chia sẻ thông tin người dùng với bên thứ ba khi chưa có sự đồng ý từ khách hàng.\n\n" +

                        "6. Quản lý thông tin xấu:\n" +
                        "   - MekongGo có quyền kiểm tra, xóa bỏ hoặc xử lý các tài khoản có hành vi vi phạm chính sách của nền tảng.\n" +
                        "   - Các nội dung sai lệch, gây ảnh hưởng đến cộng đồng hoặc vi phạm pháp luật sẽ bị xử lý nghiêm khắc.\n\n" +

                        "7. Trách nhiệm trong trường hợp phát sinh lỗi kỹ thuật:\n" +
                        "   - Trong trường hợp phát sinh lỗi hệ thống hoặc sự cố kỹ thuật làm ảnh hưởng đến quá trình giao dịch,\n" +
                        "     MekongGo sẽ nỗ lực khắc phục sự cố trong thời gian sớm nhất.\n" +
                        "   - Người dùng có thể liên hệ với bộ phận hỗ trợ để được giải quyết kịp thời.\n\n" +

                        "8. Quyền và nghĩa vụ của Ban quản lý Sàn giao dịch:\n" +
                        "   - Ban quản lý Sàn giao dịch MekongGo có trách nhiệm duy trì hoạt động ổn định của nền tảng, kiểm duyệt nội dung,\n" +
                        "     hỗ trợ khách hàng và xử lý tranh chấp giữa các bên liên quan nhằm đảm bảo môi trường giao dịch minh bạch và an toàn.\n\n" +

                        "9. Quyền và trách nhiệm của chủ xe tham gia Sàn giao dịch:\n" +
                        "   - Chủ xe có trách nhiệm cung cấp thông tin xe chính xác, minh bạch và đảm bảo xe ở trạng thái tốt khi bàn giao cho khách thuê.\n" +
                        "   - Trong trường hợp phát sinh vấn đề trong quá trình thuê, chủ xe phải phối hợp với khách thuê để giải quyết kịp thời.\n\n" +

                        "10. Điều khoản áp dụng:\n" +
                        "   - Tất cả quy định trên có hiệu lực ngay khi người dùng đăng ký tài khoản trên nền tảng MekongGo.\n" +
                        "   - Người dùng đồng ý tuân thủ các chính sách và điều khoản được MekongGo cập nhật theo thời gian.\n\n" +

                        "11. Điều khoản cam kết:\n" +
                        "   - Người dùng cam kết sử dụng dịch vụ một cách trung thực, không cố tình vi phạm hay gian lận, phá hoại hệ thống\n" +
                        "     hoặc làm ảnh hưởng đến nền tảng MekongGo.\n" +"   - Bất kỳ vi phạm nào cũng có thể dẫn đến các hình thức xử lý kỷ luật theo quy định của pháp luật.\n\n" +

                        "   Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ bộ phận hỗ trợ khách hàng của MekongGo để được giải đáp kịp thời.";

            default:
                return "Nội dung đang được cập nhật.";
        }
    }
}